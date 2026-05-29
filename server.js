﻿require('dotenv').config();

const express = require('express');
const http = require('http'); 
const { Server } = require('socket.io'); 
const mongoose = require('mongoose');
const path = require('path');
const amqp = require('amqplib');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const { v4: uuidv4 } = require('uuid');
const pino = require('pino');
const pinoHttp = require('pino-http');
const admin = require('firebase-admin');

// --- Import Routes ---
const userRoutes = require('./routes/user.routes');
const documentRoutes = require('./routes/document.routes');
const notificationRoutes = require('./routes/notification.routes');
const requestRoutes = require('./routes/request.routes'); 
const chatRoutes = require('./routes/chat.routes'); 
const reportRoutes = require('./routes/report.routes');
const categoryRoutes = require('./routes/category.routes'); // [THÊM MỚI]: Import Route Danh mục

// --- Import Models & Services ---
const Document = require('./models/Document');
const Message = require('./models/Message'); 
const { connectRabbitMQ, closeConnection } = require('./services/rabbitmq.service');

// --- Cấu hình Logger ---
const logger = pino({ level: process.env.NODE_ENV === 'production' ? 'info' : 'debug' });
const loggerMiddleware = pinoHttp({
    logger,
    genReqId: (req, res) => {
        const id = req.headers['x-request-id'] || uuidv4();
        res.setHeader('X-Request-Id', id);
        return id;
    },
    autoLogging: { ignore: (req) => req.url === '/health' || req.url === '/ready' }
});

// --- Khởi tạo Firebase Admin ---
const serviceAccount = require('./firebase-key.json');
if (!admin.apps.length) {
    admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
}

// --- Khởi tạo App & Server ---
const app = express();
const server = http.createServer(app); 

// =========================================================================
// CẤU HÌNH CORS "BẤT TỬ" ĐỂ TRỊ DỨT ĐIỂM LỖI TRÊN TRÌNH DUYỆT
// =========================================================================
const corsOptions = {
    origin: function (origin, callback) {
        // Cho phép mọi tên miền (bao gồm cả Live Server 127.0.0.1:5500) truy cập
        callback(null, true);
    },
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'], // Cho phép mọi method
    allowedHeaders: ['Content-Type', 'Authorization', 'x-request-id'], // Cấp phép header chứa Token
    credentials: true // Bắt buộc phải có để gửi Authorization Token qua Web
};

// --- Khởi tạo Socket.IO ---
const io = new Server(server, {
    cors: corsOptions
});

// --- Cấu hình Middlewares ---
app.use(loggerMiddleware);
app.use(helmet({ crossOriginResourcePolicy: { policy: 'cross-origin' } }));
app.use(cors(corsOptions)); // Áp dụng CORS mới
app.use('/api/', rateLimit({ windowMs: 15 * 60 * 1000, max: 100 }));
app.use(express.json({ limit: '25mb' }));
app.use(express.urlencoded({ limit: '25mb', extended: true }));

// =========================================================================
// QUẢN LÝ FILE TĨNH & KIỂM DUYỆT TÀI LIỆU
// =========================================================================

app.use('/uploads/thumbnails', express.static(path.join(__dirname, 'uploads', 'thumbnails')));

app.get('/uploads/:filename', async (req, res, next) => {
    try {
        const filename = req.params.filename;
        const doc = await Document.findOne({ fileUrl: '/uploads/' + filename });

        if (!doc) {
            return res.status(404).json({ message: 'File không tồn tại!' });
        }

        if (doc.status === 'verified') {
            return res.sendFile(path.join(__dirname, 'uploads', filename));
        }

        return res.status(403).json({ message: 'Tài liệu chưa được kiểm duyệt hoặc đã bị từ chối!' });
    } catch (error) {
        next(error);
    }
});

// =========================================================================
// ĐĂNG KÝ REST API ROUTES
// =========================================================================

app.use('/api/user', userRoutes);
app.use('/api', documentRoutes);
app.use('/api/notifications', notificationRoutes);
app.use('/api/requests', requestRoutes); 
app.use('/api/chat', chatRoutes);        
app.use('/api/reports', reportRoutes); 
app.use('/api/categories', categoryRoutes); // [THÊM MỚI]: Đăng ký Route Danh mục

// --- Health Checks ---
app.get('/health', (req, res) => res.status(200).json({ status: 'UP' }));
app.get('/ready', async (req, res) => {
    const isMongo = mongoose.connection.readyState === 1;
    let isRabbit = false;

    try {
        const conn = await amqp.connect(process.env.RABBITMQ_URL);
        isRabbit = true;
        await conn.close();
    } catch (_) {}

    res.status((isMongo && isRabbit) ? 200 : 503).json({ status: (isMongo && isRabbit) ? 'READY' : 'NOT_READY' });
});

// --- Middleware Bắt Lỗi Toàn Cục ---
app.use((err, req, res, next) => {
    if (err.message && err.message.startsWith('INVALID_FILE_TYPE')) {
        return res.status(400).json({ message: err.message.split(': ')[1] });
    }
    
    if (err.code === 'LIMIT_FILE_SIZE') {
        return res.status(400).json({ message: 'Dung lượng file vượt quá giới hạn cho phép.' });
    }

    if (err.message === 'CORS_NOT_ALLOWED') {
        return res.status(403).json({ message: 'Origin không được phép truy cập API.' });
    }

    req.log.error({ err }, '[UNHANDLED_ERROR]');
    res.status(err.status || 500).json({
        message: process.env.NODE_ENV === 'production' && !err.status ? 'Lỗi hệ thống!' : err.message
    });
});

// =========================================================================
// XỬ LÝ SOCKET.IO (REAL-TIME CHAT)
// =========================================================================

io.on('connection', (socket) => {
    logger.info(` Client connected to Socket: ${socket.id}`);

    // Tham gia vào phòng chat
    socket.on('join_room', (conversationId) => {
        socket.join(conversationId);
        logger.info(` User joined room: ${conversationId}`);
    });

    // Lắng nghe sự kiện gửi tin nhắn mới
    socket.on('send_message', async (data) => {
        try {
            logger.info(` Nhận được dữ liệu Socket gửi lên: ${JSON.stringify(data)}`);
            
            // Ép kiểu an toàn (trường hợp Android gửi lên JSON string thay vì Object)
            let parsedData = data;
            if (typeof data === 'string') {
                parsedData = JSON.parse(data);
            }

            const { conversationId, senderId, text } = parsedData;

            if (!conversationId || !senderId || !text) {
                logger.warn(' Dữ liệu tin nhắn bị thiếu trường (conversationId, senderId hoặc text)!');
                return;
            }

            // 1. Lưu tin nhắn vào Database
            const newMessage = await Message.create({
                conversationId,
                senderId,
                text
            });

            logger.info(` Đã lưu tin nhắn vào DB, tiến hành phát tới phòng: ${conversationId}`);

            // 2. Phát (emit) tin nhắn này tới tất cả mọi người trong phòng (Kể cả người vừa gửi)
            io.to(conversationId).emit('receive_message', newMessage);

        } catch (error) {
            logger.error(' Lỗi khi gửi qua Socket:', error);
        }
    });

    socket.on('disconnect', () => {
        logger.info(` Client disconnected: ${socket.id}`);
    });
});

// =========================================================================
// KHỔI ĐỘNG SERVER & KẾT NỐI DATABASE
// =========================================================================

const PORT = process.env.PORT || 3000;

mongoose.connect(process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/thuctaptotnghiep_db')
    .then(async () => {
        logger.info(' Đã kết nối MongoDB!');
        
        if (!process.env.RABBITMQ_URL) {
            logger.fatal('Thiếu biến môi trường RABBITMQ_URL');
            process.exit(1);
        }
        await connectRabbitMQ(logger);

        server.listen(PORT, () => {
            logger.info(` Server đang chạy tại http://localhost:${PORT}`);
        });
    })
    .catch((err) => logger.fatal({ err }, ' Lỗi kết nối MongoDB'));

// --- Xử lý khi tắt server đột ngột ---
process.on('SIGINT', async () => {
    logger.info('Đang tắt server...');
    await closeConnection(logger);
    await mongoose.connection.close();
    process.exit(0);
});