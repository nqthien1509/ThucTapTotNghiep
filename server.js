require('dotenv').config(); 

const express = require('express');
const mongoose = require('mongoose');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const amqp = require('amqplib'); 
const { body, validationResult } = require('express-validator');

// --- THƯ VIỆN BẢO MẬT & LOGGING ---
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const { v4: uuidv4 } = require('uuid');
const pino = require('pino');
const pinoHttp = require('pino-http');

const Document = require('./models/Document');
const User = require('./models/User'); 

// =======================================================
// 0. KHỞI TẠO LOGGER (PINO)
// =======================================================
const logger = pino({
    level: process.env.NODE_ENV === 'production' ? 'info' : 'debug',
    transport: process.env.NODE_ENV !== 'production' ? { target: 'pino-pretty' } : undefined,
});

const loggerMiddleware = pinoHttp({
    logger,
    genReqId: function (req, res) {
        // Lấy x-request-id từ Mobile App gửi lên, nếu không có thì tự tạo UUID mới
        const id = req.headers['x-request-id'] || uuidv4();
        res.setHeader('X-Request-Id', id);
        return id;
    },
    autoLogging: {
        // Bỏ qua log cho các route giám sát hệ thống để tránh nhiễu
        ignore: (req) => req.url === '/health' || req.url === '/ready'
    }
});

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
// 1. BẢO MẬT & MIDDLEWARE (HARDENING)
// =======================================================

// Đặt Logger lên ĐẦU TIÊN để bắt được mọi request
app.use(loggerMiddleware); 

// Dùng Helmet để thiết lập các HTTP Headers bảo mật
app.use(helmet({
    crossOriginResourcePolicy: { policy: "cross-origin" }
}));

// Cấu hình CORS Whitelist
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

// Rate Limiting: Chống Spam / DDoS nhẹ
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

// Parse dữ liệu & Static files
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
        req.log.warn({ err: error }, 'Lỗi xác thực Firebase Token');
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
            req.log.debug('Token gửi lên không hợp lệ, tiếp tục truy cập ẩn danh.');
        }
    }
    next(); 
};

// =======================================================
// 2. KẾT NỐI DATABASE & RABBITMQ
// =======================================================

// Lấy chuỗi kết nối từ file .env
const MONGO_URI = process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/thuctaptotnghiep_db';

mongoose.connect(MONGO_URI)
    .then(() => logger.info(' Đã kết nối thành công với MongoDB!'))
    .catch((err) => logger.fatal({ err }, ' Lỗi kết nối MongoDB'));

// Lấy biến RabbitMQ từ .env
const RABBITMQ_URL = process.env.RABBITMQ_URL;

if (!RABBITMQ_URL) {
    logger.fatal(' CRITICAL ERROR: Thiếu biến môi trường RABBITMQ_URL trong file .env');
    process.exit(1); 
}

// CẬP NHẬT: Thêm tham số requestId
async function sendToQueue(data, requestId) {
    try {
        const connection = await amqp.connect(RABBITMQ_URL);
        const channel = await connection.createChannel();
        const queueName = 'file_processing_queue';

        await channel.assertQueue(queueName, { durable: true });
        
        // Gắn thêm Trace ID vào payload
        const payload = { ...data, requestId: requestId };
        
        channel.sendToQueue(queueName, Buffer.from(JSON.stringify(payload)), {
            persistent: true 
        });

        logger.info({ requestId, action: data.action }, `[x] Đã gửi Job xử lý file: ${data.title}`);
        
        setTimeout(() => {
            connection.close();
        }, 500);
    } catch (error) {
        logger.error({ requestId, err: error }, ' Lỗi gửi tin nhắn đến RabbitMQ');
    }
}

// =======================================================
// 3. CẤU HÌNH LƯU TRỮ FILE (MULTER) 
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
// 4. HEALTHCHECK & READINESS ENDPOINTS (Giám sát hệ thống)
// =======================================================

// Liveness Check
app.get('/health', (req, res) => {
    res.status(200).json({
        status: 'UP',
        uptime: process.uptime(), 
        timestamp: new Date().toISOString()
    });
});

// Readiness Check
app.get('/ready', async (req, res) => {
    const isMongoConnected = mongoose.connection.readyState === 1;
    let isRabbitConnected = false;
    try {
        const connection = await amqp.connect(RABBITMQ_URL);
        isRabbitConnected = true;
        await connection.close(); 
    } catch (error) {
        logger.warn({ err: error.message }, ' [Readiness] Lỗi kết nối RabbitMQ');
    }

    const isReady = isMongoConnected && isRabbitConnected;
    const statusCode = isReady ? 200 : 503; 

    res.status(statusCode).json({
        status: isReady ? 'READY' : 'NOT_READY',
        services: {
            mongodb: isMongoConnected ? 'UP' : 'DOWN',
            rabbitmq: isRabbitConnected ? 'UP' : 'DOWN'
        },
        timestamp: new Date().toISOString()
    });
});

// =======================================================
// 5. HỆ THỐNG API CHÍNH
// =======================================================

// --- API UPLOAD TÀI LIỆU (PDF) ---
const uploadMiddleware = (req, res, next) => {
    upload.single('file')(req, res, function (err) {
        if (err instanceof multer.MulterError) {
            if (err.code === 'LIMIT_FILE_SIZE') {
                req.log.warn('Upload bị từ chối do file quá lớn');
                return res.status(400).json({ message: 'File quá lớn! Dung lượng tối đa là 10MB.' });
            }
            req.log.error({ err }, 'Lỗi Multer khi upload');
            return res.status(400).json({ message: 'Lỗi upload: ' + err.message });
        } else if (err) {
            if (err.message === 'INVALID_TYPE') {
                req.log.warn('Upload bị từ chối do sai định dạng');
                return res.status(400).json({ message: 'Chỉ cho phép tải lên định dạng file PDF!' });
            }
            req.log.error({ err }, 'Lỗi không xác định khi upload');
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
            req.log.warn({ errors: errors.array() }, 'Validate text thất bại');
            return res.status(400).json({ message: errors.array()[0].msg, errors: errors.array() });
        }

        if (!req.file) {
            req.log.warn('Không tìm thấy file tải lên');
            return res.status(400).json({ message: 'Vui lòng chọn một file PDF!' });
        }

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

        // Truyền req.id (Trace ID) vào hàm queue
        sendToQueue({
            documentId: newDoc._id,
            title: newDoc.title,
            filePath: req.file.path,
            action: 'CHECK_VIRUS_AND_THUMBNAIL',
            authorName: newDoc.authorName 
        }, req.id);

        req.log.info({ documentId: newDoc._id }, 'Upload thành công, đã lưu DB và đẩy vào hàng đợi');
        res.status(200).json({ message: 'Tài liệu đã được tải lên và đang chờ hệ thống xử lý ngầm!', document: newDoc });
    } catch (error) {
        next(error);
    }
});

// --- API QUẢN LÝ THÔNG TIN NGƯỜI DÙNG ---
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
        if (req.params.uid !== req.user.uid) {
            req.log.warn({ uidParam: req.params.uid, tokenUid: req.user.uid }, 'Thử nghiệm sửa chéo tài khoản bị chặn');
            return res.status(403).json({ message: "Forbidden: Bạn không có quyền thao tác trên tài khoản người khác!" });
        }
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

// --- API TƯƠNG TÁC NGƯỜI DÙNG ---
app.post('/api/documents/:id/favorite', verifyToken, async (req, res, next) => {
    try {
        const userId = req.user.uid; 
        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy tài liệu!' });

        // CẬP NHẬT: Dùng updateOne để bỏ qua bước validate toàn bộ file
        const isFavorited = doc.favoritedBy.includes(userId);
        if (isFavorited) {
            await Document.updateOne({ _id: req.params.id }, { $pull: { favoritedBy: userId } });
            req.log.info({ documentId: req.params.id, userId }, 'Bỏ yêu thích tài liệu');
        } else {
            await Document.updateOne({ _id: req.params.id }, { $push: { favoritedBy: userId } });
            req.log.info({ documentId: req.params.id, userId }, 'Thêm yêu thích tài liệu');
        }

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

        // CẬP NHẬT: Dùng updateOne để bỏ qua bước validate toàn bộ file
        const isWatchLater = doc.watchLaterBy.includes(userId);
        if (isWatchLater) {
            await Document.updateOne({ _id: req.params.id }, { $pull: { watchLaterBy: userId } });
            req.log.info({ documentId: req.params.id, userId }, 'Bỏ xem sau tài liệu');
        } else {
            await Document.updateOne({ _id: req.params.id }, { $push: { watchLaterBy: userId } });
            req.log.info({ documentId: req.params.id, userId }, 'Thêm xem sau tài liệu');
        }

        res.status(200).json({ message: 'Cập nhật danh sách xem sau thành công!' });
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

// --- API TRUY XUẤT DỮ LIỆU ---
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

// --- API LẤY DANH SÁCH ĐÃ LƯU ---
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

// --- XÓA TÀI LIỆU ---
app.delete('/api/documents/:id', async (req, res, next) => {
    try {
        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy!' });
        
        const filePath = path.join(__dirname, 'uploads', path.basename(doc.fileUrl));
        if (fs.existsSync(filePath)) fs.unlinkSync(filePath);
        
        await Document.findByIdAndDelete(req.params.id);
        req.log.info({ documentId: req.params.id }, 'Đã xóa tài liệu');
        res.status(200).json({ message: 'Xóa thành công!' });
    } catch (error) {
        next(error);
    }
});

app.get('/', (req, res) => res.send('Backend App Tài Liệu - API Server Bảo Mật'));

// =======================================================
// GLOBAL ERROR HANDLER
// =======================================================
app.use((err, req, res, next) => {
    // Dùng logger thay cho console.error
    req.log.error({ err }, ' [LỖI HỆ THỐNG UNHANDLED]');

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
// KHỞI ĐỘNG SERVER 
// =======================================================
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    logger.info(` Server đang chạy tại http://localhost:${PORT}`);
    logger.info(` Chế độ: ${process.env.NODE_ENV || 'development'}`);
});