require('dotenv').config(); 

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

// --- KHỞI TẠO ROUTER ---
const userRoutes = require('./routes/user.routes');
const documentRoutes = require('./routes/document.routes');

// --- LOGGER ---
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

// --- FIREBASE ---
const serviceAccount = require('./firebase-key.json'); 
if (!admin.apps.length) admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });

const app = express();

// --- BẢO MẬT & CONFIG ---
app.use(loggerMiddleware); 
app.use(helmet({ crossOriginResourcePolicy: { policy: "cross-origin" } }));
app.use(cors({ origin: '*' })); // Rút gọn để test local, hãy config lại whitelist khi lên production
app.use('/api/', rateLimit({ windowMs: 15 * 60 * 1000, max: 100 }));
app.use(express.json({ limit: '25mb'})); 
app.use(express.urlencoded({ limit: '25mb', extended: true }));
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// --- DATABASE & RABBITMQ ---
mongoose.connect(process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/thuctaptotnghiep_db')
    .then(() => logger.info(' Đã kết nối MongoDB!'))
    .catch((err) => logger.fatal({ err }, ' Lỗi MongoDB'));

if (!process.env.RABBITMQ_URL) {
    logger.fatal('Thiếu biến môi trường RABBITMQ_URL'); process.exit(1); 
}

// --- HEALTHCHECK ---
app.get('/health', (req, res) => res.status(200).json({ status: 'UP' }));
app.get('/ready', async (req, res) => {
    const isMongo = mongoose.connection.readyState === 1;
    let isRabbit = false;
    try { const conn = await amqp.connect(process.env.RABBITMQ_URL); isRabbit = true; await conn.close(); } catch(e){}
    res.status((isMongo && isRabbit) ? 200 : 503).json({ status: (isMongo && isRabbit) ? 'READY' : 'NOT_READY' });
});

// --- ĐỊNH TUYẾN API CHÍNH ---
// Note: router.use('/api') giúp giữ nguyên chính xác các endpoint cũ của Mobile App
app.use('/api/user', userRoutes);
app.use('/api', documentRoutes); 

// --- ERROR HANDLER ---
app.use((err, req, res, next) => {
    req.log.error({ err }, ' [LỖI HỆ THỐNG UNHANDLED]');
    res.status(err.status || 500).json({ message: process.env.NODE_ENV === 'production' && !err.status ? 'Lỗi hệ thống!' : err.message });
});

app.listen(process.env.PORT || 3000, () => logger.info(` Server đang chạy tại http://localhost:${process.env.PORT || 3000}`));