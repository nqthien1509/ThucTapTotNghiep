﻿require('dotenv').config();

const express = require('express');
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

const userRoutes = require('./routes/user.routes');
const documentRoutes = require('./routes/document.routes');
const Document = require('./models/Document');

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

const serviceAccount = require('./firebase-key.json');
if (!admin.apps.length) {
    admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
}

const app = express();

const corsOrigins = (process.env.CORS_ORIGINS || '')
    .split(',')
    .map((origin) => origin.trim())
    .filter(Boolean);

const corsOptions = process.env.NODE_ENV === 'production'
    ? {
        origin: (origin, callback) => {
            if (!origin || corsOrigins.includes(origin)) {
                return callback(null, true);
            }
            return callback(new Error('CORS_NOT_ALLOWED'));
        }
    }
    : { origin: true };

app.use(loggerMiddleware);
app.use(helmet({ crossOriginResourcePolicy: { policy: 'cross-origin' } }));
app.use(cors(corsOptions));
app.use('/api/', rateLimit({ windowMs: 15 * 60 * 1000, max: 100 }));
app.use(express.json({ limit: '25mb' }));
app.use(express.urlencoded({ limit: '25mb', extended: true }));

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

mongoose.connect(process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/thuctaptotnghiep_db')
    .then(() => logger.info('Da ket noi MongoDB!'))
    .catch((err) => logger.fatal({ err }, 'Loi MongoDB'));

if (!process.env.RABBITMQ_URL) {
    logger.fatal('Thieu bien moi truong RABBITMQ_URL');
    process.exit(1);
}

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

app.use('/api/user', userRoutes);
app.use('/api', documentRoutes);

app.use((err, req, res, next) => {
    // [CẬP NHẬT]: Bắt lỗi từ bộ lọc Multer (Sai định dạng)
    if (err.message && err.message.startsWith('INVALID_FILE_TYPE')) {
        return res.status(400).json({ message: err.message.split(': ')[1] });
    }
    
    // [CẬP NHẬT]: Bắt lỗi file quá lớn của Multer (Vượt dung lượng)
    if (err.code === 'LIMIT_FILE_SIZE') {
        return res.status(400).json({ message: 'Dung lượng file vượt quá giới hạn cho phép.' });
    }

    if (err.message === 'CORS_NOT_ALLOWED') {
        return res.status(403).json({ message: 'Origin khong duoc phep truy cap API.' });
    }

    req.log.error({ err }, '[UNHANDLED_ERROR]');
    res.status(err.status || 500).json({
        message: process.env.NODE_ENV === 'production' && !err.status ? 'Loi he thong!' : err.message
    });
});

app.listen(process.env.PORT || 3000, () => {
    logger.info(`Server dang chay tai http://localhost:${process.env.PORT || 3000}`);
});