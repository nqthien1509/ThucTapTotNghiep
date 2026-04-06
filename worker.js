const amqp = require('amqplib');
const mongoose = require('mongoose');
const Document = require('./models/Document');
const admin = require('firebase-admin'); // 1. Khai báo thư viện Firebase

// =======================================================
// 1. CẤU HÌNH FIREBASE ADMIN
// =======================================================
const serviceAccount = require('./firebase-key.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

// =======================================================
// 2. KẾT NỐI MONGODB
// =======================================================
mongoose.connect('mongodb://127.0.0.1:27017/thuctaptotnghiep_db')
    .then(() => console.log('✅ Worker đã kết nối MongoDB thành công!'))
    .catch(err => console.error('❌ Worker lỗi kết nối MongoDB:', err));

// 3. Link CloudAMQP
const RABBITMQ_URL = 'amqps://bkrezbsn:livqe0OmRbzLY_FxiDPV0n6nGmzooxO4@cougar.rmq.cloudamqp.com/bkrezbsn';

async function startWorker() {
    try {
        const connection = await amqp.connect(RABBITMQ_URL);
        const channel = await connection.createChannel();
        const queueName = 'file_processing_queue';

        await channel.assertQueue(queueName, { durable: true });
        channel.prefetch(1);

        console.log(' [*] Worker đã sẵn sàng. Đang chờ file mới từ sinh viên UTH...');

        channel.consume(queueName, async (msg) => {
            if (msg !== null) {
                const data = JSON.parse(msg.content.toString());
                console.log(`\n [v] Nhận Job cho ID: ${data.documentId} - Tên: ${data.title}`);
                
                // GIẢ LẬP XỬ LÝ NẶNG (Quét virus, kiểm tra định dạng...)
                console.log(' --- Hệ thống đang quét Virus và phân tích dữ liệu PDF... ---');
                await new Promise(resolve => setTimeout(resolve, 7000)); 

                try {
                    // CẬP NHẬT TRẠNG THÁI VÀO DATABASE
                    const updatedDoc = await Document.findByIdAndUpdate(
                        data.documentId, 
                        { status: 'verified' },
                        { returnDocument: 'after' } 
                    );

                    if (updatedDoc) {
                        console.log(` [OK] Đã xác thực thành công tài liệu: ${data.title}`);

                        // =======================================================
                        // GỬI THÔNG BÁO PUSH NOTIFICATION (FCM)
                        // =======================================================
                        if (data.authorName) {
                            // Tạo tên kênh (Topic) dựa trên tên người dùng (Xóa khoảng trắng)
                            const topicName = `user_${data.authorName.replace(/\s+/g, '')}`;
                            
                            const message = {
                                notification: {
                                    title: 'Kiểm duyệt hoàn tất! ✅',
                                    body: `Tài liệu "${data.title}" của bạn đã an toàn và sẵn sàng.`
                                },
                                topic: topicName // Gửi đến kênh riêng của người dùng
                            };

                            // Thực hiện bắn thông báo
                            const response = await admin.messaging().send(message);
                            console.log(' [📢] Đã gửi thông báo thành công đến:', topicName);
                        }
                    } else {
                        console.log(` [!] Không tìm thấy tài liệu ID ${data.documentId} để cập nhật.`);
                    }
                } catch (dbError) {
                    console.error(' ❌ Lỗi xử lý dữ liệu:', dbError.message);
                }

                // Báo cho RabbitMQ biết đã xong để xóa tin nhắn khỏi hàng đợi
                channel.ack(msg);
            }
        });

    } catch (error) {
        console.error('❌ Lỗi kết nối RabbitMQ:', error);
    }
}

startWorker();