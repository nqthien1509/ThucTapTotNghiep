const docRepo = require('../repositories/document.repository');
const docService = require('../services/document.service');
const { sendToQueue } = require('../services/rabbitmq.service');
const { validationResult } = require('express-validator');
const fs = require('fs');

class DocumentController {
    // ==========================================
    // 1. UPLOAD TÀI LIỆU (Chặn file rỗng 0MB)
    // ==========================================
    async upload(req, res, next) {
        try {
            // Bước 1: Kiểm tra lỗi validation từ express-validator (title, subject...)
            const errors = validationResult(req);
            if (!errors.isEmpty()) {
                // Nếu lỗi text, xóa ngay file vừa upload lên để tránh rác
                if (req.file && fs.existsSync(req.file.path)) {
                    fs.unlinkSync(req.file.path);
                }
                return res.status(400).json({ message: errors.array()[0].msg });
            }

            // Bước 2: Kiểm tra sự tồn tại của file & Lỗi 0 bytes từ máy ảo Android
            if (!req.file || req.file.size === 0) {
                // Dọn dẹp cái vỏ file rỗng nếu Multer lỡ tạo ra
                if (req.file && fs.existsSync(req.file.path)) {
                    fs.unlinkSync(req.file.path);
                }
                return res.status(400).json({ 
                    message: 'File không hợp lệ hoặc bị rỗng (0 bytes). Nếu up từ Google Drive, vui lòng tải hẳn xuống máy trước!' 
                });
            }

            // Bước 3: Chuẩn bị dữ liệu
            const { title, authorName, subject, category, description, tags } = req.body;
            const tagsArray = tags ? tags.split(',').map(t => t.trim()).filter(t => t !== "") : [];
            
            // Ghi log debug thay vì console.log để hệ thống giám sát dễ đọc
            req.log.debug({ fileName: req.file.originalname, bytes: req.file.size }, "Bắt đầu xử lý tính toán dung lượng");
            
            // Tính toán dung lượng thật từ file đã nhận
            const sizeInMB = (req.file.size / (1024 * 1024)).toFixed(2) + ' MB';
            
            // Bước 4: Lưu vào Database
            const newDoc = await docRepo.create({
                userId: req.user.uid, // Lấy từ Token bảo mật
                title: title || 'Tài liệu không tên',
                authorName: authorName || 'Người dùng Ẩn danh',
                subject: subject || 'Khác',
                category: category || 'Slide',
                description: description || '',
                tags: tagsArray,
                fileUrl: '/uploads/' + req.file.filename,
                size: sizeInMB, // Lưu dung lượng thực tế
                status: 'pending'
            });

            // Bước 5: Đẩy vào hàng đợi RabbitMQ để xử lý Thumbnail & Virus
            sendToQueue({ 
                documentId: newDoc._id, 
                title: newDoc.title, 
                filePath: req.file.path, 
                action: 'CHECK_VIRUS_AND_THUMBNAIL', 
                authorName: newDoc.authorName 
            }, req.id, req.log);

            req.log.info({ documentId: newDoc._id, size: sizeInMB }, 'Upload tài liệu thành công');
            
            res.status(200).json({ 
                message: 'Tài liệu đã được tải lên và đang chờ xử lý!', 
                document: newDoc 
            });

        } catch (error) { 
            // Nếu có lỗi bất ngờ, dọn dẹp file vật lý
            if (req.file && fs.existsSync(req.file.path)) {
                fs.unlinkSync(req.file.path);
            }
            next(error); 
        }
    }

    // ==========================================
    // 2. TƯƠNG TÁC (YÊU THÍCH / XEM SAU)
    // ==========================================
    async toggleFavorite(req, res, next) {
        try {
            const isAdded = await docService.toggleFavorite(req.params.id, req.user.uid);
            res.status(200).json({ message: isAdded ? 'Đã thêm vào yêu thích' : 'Đã xóa khỏi yêu thích' });
        } catch (error) { next(error); }
    }

    async toggleWatchLater(req, res, next) {
        try {
            const isAdded = await docService.toggleWatchLater(req.params.id, req.user.uid);
            res.status(200).json({ message: isAdded ? 'Đã thêm vào danh sách xem sau' : 'Đã xóa khỏi danh sách xem sau' });
        } catch (error) { next(error); }
    }

    // ==========================================
    // 3. TRUY XUẤT DỮ LIỆU
    // ==========================================
    async getAll(req, res, next) {
        try { 
            const docs = await docRepo.findAll();
            res.status(200).json(docs); 
        } catch (error) { next(error); }
    }

    async getDetail(req, res, next) {
        try {
            const doc = await docRepo.findById(req.params.id);
            if (!doc) return res.status(404).json({ message: 'Không tìm thấy tài liệu!' });

            const docData = doc.toObject();
            // Kiểm tra trạng thái của User hiện tại với tài liệu này
            docData.isFavorite = req.user ? doc.favoritedBy.includes(req.user.uid) : false;
            docData.isWatchLater = req.user ? doc.watchLaterBy.includes(req.user.uid) : false;
            
            // Bảo mật: Không trả về mảng ID của hàng nghìn người khác
            delete docData.favoritedBy; 
            delete docData.watchLaterBy;

            res.status(200).json(docData);
        } catch (error) { next(error); }
    }

    async search(req, res, next) {
        try {
            let query = {};
            if (req.query.q) query.title = { $regex: req.query.q, $options: 'i' };
            if (req.query.category && req.query.category !== 'Tất cả') query.category = req.query.category;
            
            // Nếu không có từ khóa và không có category, trả về mảng rỗng thay vì lỗi
            if (Object.keys(query).length === 0) return res.status(200).json([]);
            
            const results = await docRepo.findByQuery(query);
            res.status(200).json(results);
        } catch (error) { next(error); }
    }

    async getMyDocuments(req, res, next) {
        try { 
            // Tìm tài liệu dựa trên userId lấy từ Token
            const docs = await docRepo.findByQuery({ userId: req.user.uid });
            res.status(200).json(docs); 
        } catch (error) { next(error); }
    }

    async getFavorites(req, res, next) {
        try {
            if (req.params.userId !== req.user.uid) return res.status(403).json({ message: "Forbidden" });
            const docs = await docRepo.findByQuery({ favoritedBy: req.params.userId });
            res.status(200).json(docs);
        } catch (error) { next(error); }
    }

    async getWatchLater(req, res, next) {
        try {
            if (req.params.userId !== req.user.uid) return res.status(403).json({ message: "Forbidden" });
            const docs = await docRepo.findByQuery({ watchLaterBy: req.params.userId });
            res.status(200).json(docs);
        } catch (error) { next(error); }
    }

    // ==========================================
    // 4. XÓA TÀI LIỆU (IDOR Prevention)
    // ==========================================
    async delete(req, res, next) {
        try {
            await docService.deleteDocument(req.params.id, req.user.uid);
            req.log.info({ docId: req.params.id }, 'Xóa tài liệu thành công');
            res.status(200).json({ message: 'Xóa thành công!' });
        } catch (error) {
            if (error.message === 'FORBIDDEN') return res.status(403).json({ message: 'Bạn không có quyền xóa tài liệu này!' });
            if (error.message === 'NOT_FOUND') return res.status(404).json({ message: 'Tài liệu không tồn tại!' });
            next(error);
        }
    }
}

module.exports = new DocumentController();