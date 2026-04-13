require('dotenv').config(); // <-- CHÚ Ý: Phải luôn nằm ở dòng đầu tiên

const express = require('express');
const mongoose = require('mongoose');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const amqp = require('amqplib'); 
const { body, validationResult } = require('express-validator');

// --- THƯ VIỆN BẢO MẬT ---
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');

const Document = require('./models/Document');
const User = require('./models/User'); 

// =======================================================
// FIREBASE ADMIN INITIALIZATION
// =======================================================
const admin = require('firebase-admin');
const serviceAccount = require('./firebase-key.json'); 

if (!admin.apps.length) {
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });
}

const app = express();

// =======================================================
// 0. BẢO MẬT & MIDDLEWARE (HARDENING)
// =======================================================

// 1. Dùng Helmet để thiết lập các HTTP Headers bảo mật
app.use(helmet({
    crossOriginResourcePolicy: { policy: "cross-origin" }
}));

// 2. Cấu hình CORS Whitelist
const whitelist = process.env.NODE_ENV === 'production' 
    ? ['https://domain-thuc-te-cua-ban.com'] 
    : ['http://localhost:3000', 'http://127.0.0.1:3000']; 

const corsOptions = {
    origin: function (origin, callback) {
        if (!origin || whitelist.indexOf(origin) !== -1) {
            callback(null, true);
        } else {
            callback(new Error('Khóa bởi CORS - Truy cập bị từ chối'));
        }
    }
};
app.use(cors(corsOptions));

// 3. Rate Limiting: Chống Spam / DDoS nhẹ
const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, 
    max: 100, 
    standardHeaders: true, 
    legacyHeaders: false,
    message: { 
        message: 'Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 15 phút!' 
    }
});
app.use('/api/', apiLimiter);

// 4. Parse dữ liệu & Static files
app.use(express.json({ limit: '50kb' })); 
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// =======================================================
// MIDDLEWARE XÁC THỰC
// =======================================================

const verifyToken = async (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ message: 'Unauthorized: Thiếu hoặc sai định dạng token!' });
    }

    const token = authHeader.split(' ')[1];
    try {
        const decodedToken = await admin.auth().verifyIdToken(token);
        req.user = decodedToken; 
        next();
    } catch (error) {
        console.error('Lỗi xác thực Firebase Token:', error);
        return res.status(401).json({ message: 'Unauthorized: Token không hợp lệ hoặc đã hết hạn!' });
    }
};

const optionalVerifyToken = async (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.split(' ')[1];
        try {
            const decodedToken = await admin.auth().verifyIdToken(token);
            req.user = decodedToken; 
        } catch (error) {
            console.error('Token gửi lên không hợp lệ, tiếp tục truy cập ẩn danh.');
        }
    }
    next(); 
};

// =======================================================
// 1. KẾT NỐI DATABASE & RABBITMQ (ĐÃ THÁO HARDCODE)
// =======================================================

// Lấy chuỗi kết nối từ file .env
const MONGO_URI = process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/thuctaptotnghiep_db';

mongoose.connect(MONGO_URI)
    .then(() => console.log('✅ Đã kết nối thành công với MongoDB!'))
    .catch((err) => console.error('❌ Lỗi kết nối MongoDB:', err));

// Lấy biến RabbitMQ từ .env và check an toàn
const RABBITMQ_URL = process.env.RABBITMQ_URL;

if (!RABBITMQ_URL) {
    console.error('❌ CRITICAL ERROR: Thiếu biến môi trường RABBITMQ_URL trong file .env');
    process.exit(1); // Dừng server ngay lập tức để tránh lỗi ngầm
}

async function sendToQueue(data) {
    try {
        const connection = await amqp.connect(RABBITMQ_URL);
        const channel = await connection.createChannel();
        const queueName = 'file_processing_queue';

        await channel.assertQueue(queueName, { durable: true });
        
        channel.sendToQueue(queueName, Buffer.from(JSON.stringify(data)), {
            persistent: true 
        });

        console.log(` [x] Đã gửi Job xử lý file: ${data.title}`);
        
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

const upload = multer({ 
    storage: storage,
    limits: { fileSize: 10 * 1024 * 1024 },
    fileFilter: (req, file, cb) => {
        if (file.mimetype === 'application/pdf') cb(null, true);
        else cb(new Error('INVALID_TYPE'), false);
    }
});

// =======================================================
// 3. HỆ THỐNG API
// =======================================================

// --- API UPLOAD TÀI LIỆU (PDF) ---
const uploadMiddleware = (req, res, next) => {
    upload.single('file')(req, res, function (err) {
        if (err instanceof multer.MulterError) {
            if (err.code === 'LIMIT_FILE_SIZE') return res.status(400).json({ message: 'File quá lớn! Dung lượng tối đa là 10MB.' });
            return res.status(400).json({ message: 'Lỗi upload: ' + err.message });
        } else if (err) {
            if (err.message === 'INVALID_TYPE') return res.status(400).json({ message: 'Chỉ cho phép tải lên định dạng file PDF!' });
            return res.status(400).json({ message: err.message });
        }
        next();
    });
};

app.post('/api/upload', 
    uploadMiddleware,
    [
        body('title').notEmpty().withMessage('Tiêu đề không được để trống').isLength({ max: 100 }).withMessage('Tiêu đề tối đa 100 ký tự'),
        body('subject').notEmpty().withMessage('Môn học không được để trống').isLength({ max: 50 }).withMessage('Môn học tối đa 50 ký tự'),
        body('category').isIn(['Slide', 'Đề thi', 'Giáo trình']).withMessage('Loại tài liệu không hợp lệ'),
        body('description').optional().isLength({ max: 500 }).withMessage('Mô tả tối đa 500 ký tự'),
        body('tags').optional().isLength({ max: 200 }).withMessage('Chuỗi tags quá dài')
    ],
    async (req, res, next) => {
    try {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            if (req.file && fs.existsSync(req.file.path)) fs.unlinkSync(req.file.path); 
            return res.status(400).json({ message: errors.array()[0].msg, errors: errors.array() });
        }

        if (!req.file) return res.status(400).json({ message: 'Vui lòng chọn một file PDF!' });

        const { title, authorName, subject, category, description, tags } = req.body;
        const tagsArray = tags ? tags.split(',').map(tag => tag.trim()).filter(tag => tag !== "") : [];
        const sizeInMB = (req.file.size / (1024 * 1024)).toFixed(2) + ' MB';

        const newDoc = new Document({
            title: title || 'Tài liệu không tên',
            authorName: authorName || 'Người dùng Ẩn danh',
            subject: subject || 'Khác',
            category: category || 'Slide',
            description: description || '',
            tags: tagsArray,
            fileUrl: '/uploads/' + req.file.filename,
            size: sizeInMB,
            status: 'pending' 
        });

        await newDoc.save();

        sendToQueue({
            documentId: newDoc._id,
            title: newDoc.title,
            filePath: req.file.path,
            action: 'CHECK_VIRUS_AND_THUMBNAIL',
            authorName: newDoc.authorName 
        });

        res.status(200).json({ message: 'Tài liệu đã được tải lên và đang chờ hệ thống xử lý ngầm!', document: newDoc });
    } catch (error) {
        next(error);
    }
});

// --- CÁC API KHÁC (Giữ nguyên logic) ---
app.get('/api/user/:uid', async (req, res, next) => {
    try {
        let user = await User.findById(req.params.uid);
        if (!user) {
            return res.status(200).json({ _id: req.params.uid, email: "", displayName: "", school: "Trường Đại học Giao thông vận tải TP.HCM (UTH)", bio: "", avatarUrl: "" });
        }
        res.status(200).json(user);
    } catch (error) {
        next(error);
    }
});

app.put('/api/user/:uid', verifyToken, async (req, res, next) => {
    try {
        if (req.params.uid !== req.user.uid) return res.status(403).json({ message: "Forbidden: Bạn không có quyền thao tác trên tài khoản người khác!" });
        const { email, displayName, school, bio } = req.body;
        const updatedUser = await User.findByIdAndUpdate(req.params.uid, { email, displayName, school, bio }, { new: true, upsert: true });
        res.status(200).json(updatedUser);
    } catch (error) {
        next(error);
    }
});

app.post('/api/user/:uid/avatar', verifyToken, upload.single('avatar'), async (req, res, next) => {
    try {
        if (req.params.uid !== req.user.uid) return res.status(403).json({ message: "Forbidden: Bạn không có quyền thao tác trên tài khoản người khác!" });
        if (!req.file) return res.status(400).json({ message: 'Vui lòng chọn một file ảnh!' });

        const newAvatarUrl = '/uploads/' + req.file.filename;
        const updatedUser = await User.findByIdAndUpdate(req.params.uid, { avatarUrl: newAvatarUrl }, { new: true, upsert: true });
        res.status(200).json(updatedUser);
    } catch (error) {
        next(error);
    }
});

app.post('/api/documents/:id/favorite', verifyToken, async (req, res, next) => {
    try {
        const userId = req.user.uid; 
        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy tài liệu!' });

        const index = doc.favoritedBy.indexOf(userId);
        if (index === -1) doc.favoritedBy.push(userId); 
        else doc.favoritedBy.splice(index, 1); 

        await doc.save();
        res.status(200).json({ message: 'Cập nhật trạng thái yêu thích thành công!' });
    } catch (error) {
        next(error);
    }
});

app.post('/api/documents/:id/watch-later', verifyToken, async (req, res, next) => {
    try {
        const userId = req.user.uid; 
        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy tài liệu!' });

        const index = doc.watchLaterBy.indexOf(userId);
        if (index === -1) doc.watchLaterBy.push(userId); 
        else doc.watchLaterBy.splice(index, 1);

        await doc.save();
        res.status(200).json({ message: 'Cập nhật danh sách xem sau thành công!' });
    } catch (error) {
        next(error);
    }
});

app.get('/api/documents', async (req, res, next) => {
    try {
        const documents = await Document.find().sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        next(error);
    }
});

app.get('/api/documents/:id', optionalVerifyToken, async (req, res, next) => {
    try {
        const userId = req.user ? req.user.uid : null; 
        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy!' });

        const docData = doc.toObject();
        docData.isFavorite = userId ? doc.favoritedBy.includes(userId) : false;
        docData.isWatchLater = userId ? doc.watchLaterBy.includes(userId) : false;
        delete docData.favoritedBy;
        delete docData.watchLaterBy;

        res.status(200).json(docData);
    } catch (error) {
        next(error);
    }
});

app.get('/api/search', async (req, res, next) => {
    try {
        const { q, category } = req.query;
        let queryObj = {};

        if (q) queryObj.title = { $regex: q, $options: 'i' }; 
        if (category && category !== 'Tất cả') queryObj.category = category; 
        if (!q && (!category || category === 'Tất cả')) return res.status(200).json([]);

        const documents = await Document.find(queryObj).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        next(error);
    }
});

app.get('/api/my-documents/:authorName', async (req, res, next) => {
    try {
        const documents = await Document.find({ authorName: req.params.authorName }).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        next(error);
    }
});

app.get('/api/users/:userId/favorites', verifyToken, async (req, res, next) => {
    try {
        if (req.params.userId !== req.user.uid) return res.status(403).json({ message: "Forbidden: Bạn không có quyền xem dữ liệu của người khác!" });
        const documents = await Document.find({ favoritedBy: req.params.userId }).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        next(error);
    }
});

app.get('/api/users/:userId/watch-later', verifyToken, async (req, res, next) => {
    try {
        if (req.params.userId !== req.user.uid) return res.status(403).json({ message: "Forbidden: Bạn không có quyền xem dữ liệu của người khác!" });
        const documents = await Document.find({ watchLaterBy: req.params.userId }).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        next(error);
    }
});

app.delete('/api/documents/:id', async (req, res, next) => {
    try {
        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy!' });
        
        const filePath = path.join(__dirname, 'uploads', path.basename(doc.fileUrl));
        if (fs.existsSync(filePath)) fs.unlinkSync(filePath);
        
        await Document.findByIdAndDelete(req.params.id);
        res.status(200).json({ message: 'Xóa thành công!' });
    } catch (error) {
        next(error);
    }
});

app.get('/', (req, res) => res.send('Backend App Tài Liệu - Được bảo vệ toàn diện'));

// =======================================================
// GLOBAL ERROR HANDLER
// =======================================================
app.use((err, req, res, next) => {
    console.error('🔥 [LỖI HỆ THỐNG]:', err);

    const statusCode = err.status || 500;
    let responseMessage = err.message;
    
    if (process.env.NODE_ENV === 'production' && statusCode === 500) {
        responseMessage = 'Đã xảy ra lỗi hệ thống nội bộ. Vui lòng thử lại sau!';
    }

    const errorResponse = { message: responseMessage };

    if (process.env.NODE_ENV !== 'production') {
        errorResponse.stack = err.stack;
    }

    res.status(statusCode).json(errorResponse);
});

// =======================================================
// KHỞI ĐỘNG SERVER (Lấy PORT từ .env)
// =======================================================
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`🚀 Server đang chạy tại http://localhost:${PORT}`);
    console.log(`🌍 Chế độ: ${process.env.NODE_ENV}`);
});