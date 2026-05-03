﻿const docRepo = require('../repositories/document.repository');
const docService = require('../services/document.service');
const { sendToQueue } = require('../services/rabbitmq.service');
const { notifyDocumentFavorited } = require('../services/notification.service');
const { validationResult } = require('express-validator');
const fs = require('fs');
const path = require('path');
const { fromPath } = require('pdf2pic'); // [MỚI] Import thư viện cắt ảnh PDF

class DocumentController {
    async upload(req, res, next) {
        let createdDoc = null;

        try {
            const errors = validationResult(req);
            if (!errors.isEmpty()) {
                if (req.file && fs.existsSync(req.file.path)) {
                    fs.unlinkSync(req.file.path);
                }
                return res.status(400).json({ message: errors.array()[0].msg });
            }

            if (!req.file || req.file.size === 0) {
                if (req.file && fs.existsSync(req.file.path)) {
                    fs.unlinkSync(req.file.path);
                }
                return res.status(400).json({
                    message: 'File khong hop le hoac bi rong (0 bytes).'
                });
            }

            const { title, authorName, subject, category, description, tags } = req.body;
            const tagsArray = tags ? tags.split(',').map((t) => t.trim()).filter((t) => t !== '') : [];

            req.log.debug({ fileName: req.file.originalname, bytes: req.file.size }, 'Bat dau xu ly tinh toan dung luong');

            const sizeInMB = (req.file.size / (1024 * 1024)).toFixed(2) + ' MB';

            // ==========================================
            // [MỚI]: XỬ LÝ TRÍCH XUẤT THUMBNAIL (Trang 1)
            // ==========================================
            let finalThumbnailUrl = null;
            try {
                // Tạo thư mục uploads/thumbnails nếu chưa tồn tại
                const thumbDir = path.join(process.cwd(), 'uploads', 'thumbnails');
                if (!fs.existsSync(thumbDir)) {
                    fs.mkdirSync(thumbDir, { recursive: true });
                }

                // Cấu hình thư viện pdf2pic
                const options = {
                    density: 100, // Độ sắc nét
                    saveFilename: `thumb_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
                    savePath: thumbDir,
                    format: "png",
                    width: 600,
                    height: 800
                };

                const storeAsImage = fromPath(req.file.path, options);
                
                // Trích xuất trang 1 (index 1 trong pdf2pic)
                const data = await storeAsImage(1, { responseType: "image" }); 
                finalThumbnailUrl = `/uploads/thumbnails/${data.name}`;
            } catch (thumbErr) {
                req.log.warn({ err: thumbErr }, 'Khong the tao thumbnail, se tiep tuc luu document khong co anh bia');
            }
            // ==========================================

            createdDoc = await docRepo.create({
                userId: req.user.uid,
                title: title || 'Tai lieu khong ten',
                authorName: authorName || 'Nguoi dung an danh',
                subject: subject || 'Khac',
                category: category || 'Slide',
                description: description || '',
                tags: tagsArray,
                fileUrl: '/uploads/' + req.file.filename,
                thumbnailUrl: finalThumbnailUrl, // [CẬP NHẬT] Lưu đường dẫn ảnh vào DB
                size: sizeInMB,
                status: 'pending'
            });

            // Gửi vào hàng đợi RabbitMQ để quét virus hoặc các tác vụ nền khác
            await sendToQueue({
                documentId: createdDoc._id,
                title: createdDoc.title,
                filePath: req.file.path,
                action: 'CHECK_VIRUS_AND_THUMBNAIL',
                userId: req.user.uid
            }, req.id, req.log);

            req.log.info({ documentId: createdDoc._id, size: sizeInMB }, 'Upload tai lieu thanh cong');

            res.status(200).json({
                message: 'Tai lieu da duoc tai len va dang cho xu ly!',
                document: createdDoc
            });
        } catch (error) {
            if (req.file && fs.existsSync(req.file.path)) {
                fs.unlinkSync(req.file.path);
            }

            if (createdDoc && createdDoc._id) {
                try {
                    await docRepo.deleteById(createdDoc._id);
                } catch (rollbackError) {
                    req.log.error({ err: rollbackError, documentId: createdDoc._id }, 'Rollback document that bai');
                }
            }

            if (!error.status) {
                error.status = 503;
                error.message = 'Khong the day job vao hang doi. Vui long thu lai sau.';
            }

            next(error);
        }
    }

    async toggleFavorite(req, res, next) {
        try {
            const result = await docService.toggleFavorite(req.params.id, req.user.uid);
            const isAdded = result.isAdded;

            if (isAdded) {
                try {
                    await notifyDocumentFavorited({
                        ownerUid: result.document.userId,
                        likerUid: req.user.uid,
                        documentId: result.document._id,
                        documentTitle: result.document.title,
                        logger: req.log
                    });
                } catch (notifyError) {
                    req.log.error({ err: notifyError, documentId: req.params.id }, 'Gui thong bao like that bai');
                }
            }

            res.status(200).json({ message: isAdded ? 'Da them vao yeu thich' : 'Da xoa khoi yeu thich' });
        } catch (error) { next(error); }
    }

    async toggleWatchLater(req, res, next) {
        try {
            const isAdded = await docService.toggleWatchLater(req.params.id, req.user.uid);
            res.status(200).json({ message: isAdded ? 'Da them vao danh sach xem sau' : 'Da xoa khoi danh sach xem sau' });
        } catch (error) { next(error); }
    }

    async getAll(req, res, next) {
        try {
            const docs = await docRepo.findByQuery({ status: 'verified' });
            res.status(200).json(docs);
        } catch (error) { next(error); }
    }

    async getDetail(req, res, next) {
        try {
            const doc = await docRepo.findById(req.params.id);
            if (!doc) return res.status(404).json({ message: 'Khong tim thay tai lieu!' });

            if (doc.status !== 'verified') {
                if (!req.user || req.user.uid !== doc.userId) {
                    return res.status(403).json({ message: 'Tài liệu đang chờ kiểm duyệt và bạn không có quyền xem.' });
                }
            }

            const docData = doc.toObject();
            docData.isFavorite = req.user ? doc.favoritedBy.includes(req.user.uid) : false;
            docData.isWatchLater = req.user ? doc.watchLaterBy.includes(req.user.uid) : false;

            delete docData.favoritedBy;
            delete docData.watchLaterBy;

            res.status(200).json(docData);
        } catch (error) { next(error); }
    }

    async search(req, res, next) {
        try {
            const query = {};
            if (req.query.q) query.title = { $regex: req.query.q, $options: 'i' };
            if (req.query.category && req.query.category !== 'Tat ca') query.category = req.query.category;

            query.status = 'verified';

            if (Object.keys(query).length === 1) return res.status(200).json([]);

            const results = await docRepo.findByQuery(query);
            res.status(200).json(results);
        } catch (error) { next(error); }
    }

    async getMyDocuments(req, res, next) {
        try {
            const docs = await docRepo.findByQuery({ userId: req.user.uid });
            res.status(200).json(docs);
        } catch (error) { next(error); }
    }

    async getFavorites(req, res, next) {
        try {
            if (req.params.userId !== req.user.uid) return res.status(403).json({ message: 'Forbidden' });
            const docs = await docRepo.findByQuery({ favoritedBy: req.params.userId, status: 'verified' });
            res.status(200).json(docs);
        } catch (error) { next(error); }
    }

    async getWatchLater(req, res, next) {
        try {
            if (req.params.userId !== req.user.uid) return res.status(403).json({ message: 'Forbidden' });
            const docs = await docRepo.findByQuery({ watchLaterBy: req.params.userId, status: 'verified' });
            res.status(200).json(docs);
        } catch (error) { next(error); }
    }

    async delete(req, res, next) {
        try {
            await docService.deleteDocument(req.params.id, req.user.uid);
            req.log.info({ docId: req.params.id }, 'Xoa tai lieu thanh cong');
            res.status(200).json({ message: 'Xoa thanh cong!' });
        } catch (error) {
            if (error.message === 'FORBIDDEN') return res.status(403).json({ message: 'Ban khong co quyen xoa tai lieu nay!' });
            if (error.message === 'NOT_FOUND') return res.status(404).json({ message: 'Tai lieu khong ton tai!' });
            next(error);
        }
    }
}

module.exports = new DocumentController();