require('dotenv').config();

const amqp = require('amqplib');
const mongoose = require('mongoose');
const Document = require('./models/Document');
const admin = require('firebase-admin');

// =======================================================
// 0. KHỞI TẠO LOGGER (PINO)
// =======================================================
const pino = require('pino');
const logger = pino({
    level: process.env.NODE_ENV === 'production' ? 'info' : 'debug',
    transport: process.env.NODE_ENV !== 'production' ? { target: 'pino-pretty' } : undefined,
});

// =======================================================
// 1. CẤU HÌNH FIREBASE ADMIN
// =======================================================
const serviceAccount = require('./firebase-key.json');

if (!admin.apps.length) {
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });
}

// =======================================================
// 2. KẾT NỐI MONGODB (Dùng biến môi trường)
// =======================================================
const MONGO_URI = process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/thuctaptotnghiep_db';

mongoose.connect(MONGO_URI)
    .then(() => logger.info(' Worker đã kết nối MongoDB thành công!'))
    .catch(err => {
        logger.fatal({ err }, ' Worker lỗi kết nối MongoDB');
        process.exit(1); 
    });

// =======================================================
// 3. KẾT NỐI RABBITMQ VÀ XỬ LÝ JOB
// =======================================================
const RABBITMQ_URL = process.env.RABBITMQ_URL;

if (!RABBITMQ_URL) {
    logger.fatal(' CRITICAL ERROR: Thiếu biến môi trường RABBITMQ_URL trong file .env');
    process.exit(1);
}

async function startWorker() {
    try {
        const connection = await amqp.connect(RABBITMQ_URL);
        const channel = await connection.createChannel();
        const queueName = 'file_processing_queue';

        await channel.assertQueue(queueName, { durable: true });
        
        // Chỉ nhận 1 job mỗi lần, làm xong mới nhận tiếp
        channel.prefetch(1);

        logger.info(' [*] Worker đã sẵn sàng. Đang chờ file mới...');

        channel.consume(queueName, async (msg) => {
            if (msg !== null) {
                const data = JSON.parse(msg.content.toString());
                
                // BÓC TÁCH TRACE ID (requestId) TỪ PAYLOAD
                const { documentId, title, authorName, requestId } = data; 

                // Gắn requestId vào mọi dòng log của job này
                logger.info({ requestId, documentId }, `[v] Nhận Job xử lý tài liệu: ${title}`);
                logger.debug({ requestId, documentId }, '--- Hệ thống đang giả lập quét Virus và phân tích PDF... ---');
                
                // GIẢ LẬP XỬ LÝ NẶNG
                await new Promise(resolve => setTimeout(resolve, 7000)); 

                try {
                    // CẬP NHẬT TRẠNG THÁI VÀO DATABASE
                    const updatedDoc = await Document.findByIdAndUpdate(
                        documentId, 
                        { status: 'verified' },
                        { returnDocument: 'after' } 
                    );

                    if (updatedDoc) {
                        logger.info({ requestId, documentId }, `[OK] Đã xác thực thành công tài liệu: ${title}`);

                        // =======================================================
                        // GỬI THÔNG BÁO PUSH NOTIFICATION (FCM)
                        // =======================================================
                        if (authorName) {
                            const topicName = `user_${authorName.replace(/\s+/g, '')}`;
                            
                            const message = {
                                notification: {
                                    title: 'Kiểm duyệt hoàn tất! ',
                                    body: `Tài liệu "${title}" của bạn đã an toàn và sẵn sàng để chia sẻ.`
                                },
                                data: {
                                    documentId: documentId.toString(),
                                    action: 'OPEN_DOCUMENT_DETAIL'
                                },
                                topic: topicName
                            };

                            await admin.messaging().send(message);
                            logger.info({ requestId, documentId, topicName }, `[] Đã gửi thông báo thành công đến topic: ${topicName}`);
                        }
                    } else {
                        logger.warn({ requestId, documentId }, `[!] Không tìm thấy tài liệu ID ${documentId} để cập nhật.`);
                    }
                } catch (dbError) {
                    logger.error({ requestId, documentId, err: dbError }, ' Lỗi xử lý dữ liệu DB hoặc gửi thông báo FCM');
                } finally {
                    // Báo cho RabbitMQ biết đã xong để xóa tin nhắn khỏi hàng đợi
                    channel.ack(msg);
                }
            }
        });

    } catch (error) {
        logger.error({ err: error }, ' Lỗi kết nối RabbitMQ. Đang thử lại...');
        // Tự động thử kết nối lại sau 5 giây nếu rớt mạng
        setTimeout(startWorker, 5000);
    }
}

startWorker();