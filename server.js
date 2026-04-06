const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const amqp = require('amqplib'); // Thư viện kết nối RabbitMQ
const Document = require('./models/Document');

const app = express();

app.use(cors());
app.use(express.json());
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// =======================================================
// 1. KẾT NỐI DATABASE & RABBITMQ
// =======================================================
mongoose.connect('mongodb://127.0.0.1:27017/thuctaptotnghiep_db')
    .then(() => console.log('✅ Đã kết nối thành công với MongoDB!'))
    .catch((err) => console.error('❌ Lỗi kết nối MongoDB:', err));

// DÁN LINK CLOUDAMQP CỦA BẠN VÀO ĐÂY 👇
const RABBITMQ_URL = 'amqps://bkrezbsn:livqe0OmRbzLY_FxiDPV0n6nGmzooxO4@cougar.rmq.cloudamqp.com/bkrezbsn';

// Hàm gửi tin nhắn vào hàng đợi (Producer)
async function sendToQueue(data) {
    try {
        const connection = await amqp.connect(RABBITMQ_URL);
        const channel = await connection.createChannel();
        const queueName = 'file_processing_queue';

        // Đảm bảo hàng đợi tồn tại
        await channel.assertQueue(queueName, { durable: true });
        
        // Gửi dữ liệu dưới dạng Buffer
        channel.sendToQueue(queueName, Buffer.from(JSON.stringify(data)), {
            persistent: true // Tin nhắn sẽ không bị mất nếu server RabbitMQ khởi động lại
        });

        console.log(` [x] Đã gửi Job xử lý file: ${data.title}`);
        
        // Đóng kết nối sau khi gửi xong
        setTimeout(() => {
            connection.close();
        }, 500);
    } catch (error) {
        console.error('❌ Lỗi gửi tin nhắn đến RabbitMQ:', error);
    }
}

// =======================================================
// 2. CẤU HÌNH LƯU TRỮ FILE (MULTER)
// =======================================================
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir);
}

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, 'uploads/'),
    filename: (req, file, cb) => cb(null, Date.now() + '-' + file.originalname)
});
const upload = multer({ storage: storage });

// =======================================================
// 3. HỆ THỐNG API (ĐÃ CẬP NHẬT API UPLOAD)
// =======================================================

// API 1: UPLOAD (ĐÃ TÍCH HỢP RABBITMQ VÀ CHUẨN BỊ CHO FIREBASE FCM)
app.post('/api/upload', upload.single('file'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ message: 'Vui lòng chọn một file PDF!' });
        }

        const { title, authorName } = req.body;
        const sizeInMB = (req.file.size / (1024 * 1024)).toFixed(2) + ' MB';

        // Lưu thông tin vào MongoDB trước
        const newDoc = new Document({
            title: title || 'Tài liệu không tên',
            authorName: authorName || 'Người dùng Ẩn danh',
            fileUrl: '/uploads/' + req.file.filename,
            size: sizeInMB,
            status: 'pending' // GIỮ LẠI LỆNH NÀY ĐỂ ÉP MONGODB LƯU TRẠNG THÁI
        });

        await newDoc.save();

        // ĐẨY THÔNG TIN FILE VÀO RABBITMQ ĐỂ XỬ LÝ NGẦM
        sendToQueue({
            documentId: newDoc._id,
            title: newDoc.title,
            filePath: req.file.path,
            action: 'CHECK_VIRUS_AND_THUMBNAIL',
            authorName: newDoc.authorName // <--- ĐIỂM MỚI: Bỏ thêm tên vào để Worker biết gửi thông báo cho ai
        });

        // TRẢ VỀ KẾT QUẢ NGAY LẬP TỨC CHO APP ANDROID
        res.status(200).json({
            message: 'Tài liệu đã được tải lên và đang chờ hệ thống xử lý ngầm!',
            document: newDoc
        });

    } catch (error) {
        console.error('Lỗi khi upload:', error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// --- GIỮ NGUYÊN CÁC API KHÁC CỦA BẠN (Documents, Search, Delete...) ---
app.get('/api/documents', async (req, res) => {
    try {
        const documents = await Document.find().sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

app.get('/api/documents/:id', async (req, res) => {
    try {
        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy!' });
        res.status(200).json(doc);
    } catch (error) {
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

app.get('/api/search', async (req, res) => {
    try {
        const keyword = req.query.q;
        if (!keyword) return res.status(200).json([]);
        const documents = await Document.find({
            title: { $regex: keyword, $options: 'i' }
        }).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

app.get('/api/my-documents/:authorName', async (req, res) => {
    try {
        const documents = await Document.find({ authorName: req.params.authorName }).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

app.delete('/api/documents/:id', async (req, res) => {
    try {
        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy!' });
        const filePath = path.join(__dirname, 'uploads', path.basename(doc.fileUrl));
        if (fs.existsSync(filePath)) fs.unlinkSync(filePath);
        await Document.findByIdAndDelete(req.params.id);
        res.status(200).json({ message: 'Xóa thành công!' });
    } catch (error) {
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

app.get('/', (req, res) => res.send('Backend App Tài Liệu + RabbitMQ 🚀'));

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`🚀 Server đang chạy tại http://localhost:${PORT}`);
});