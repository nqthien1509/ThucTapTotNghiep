﻿const amqp = require('amqplib');

const QUEUE_NAME = 'file_processing_queue';

// Biến global lưu trữ connection và channel dùng chung
let connection = null;
let channel = null;
let isConnecting = false; // Cờ (flag) để tránh gọi hàm connect nhiều lần cùng lúc

/**
 * Khởi tạo kết nối tới RabbitMQ (Chỉ gọi 1 lần khi start server)
 */
async function connectRabbitMQ(logger = console) {
    if (isConnecting) return;
    isConnecting = true;

    try {
        const amqpUrl = process.env.RABBITMQ_URL || 'amqp://localhost';
        connection = await amqp.connect(amqpUrl);

        // Bắt sự kiện lỗi kết nối
        connection.on('error', (err) => {
            logger.error({ err }, '[RabbitMQ] Connection error');
        });

        // Logic Auto-reconnect khi kết nối bị đóng
        connection.on('close', () => {
            logger.warn('[RabbitMQ] Connection closed. Đang thử kết nối lại sau 5s...');
            connection = null;
            channel = null;
            isConnecting = false;
            setTimeout(() => connectRabbitMQ(logger), 5000);
        });

        // Tạo channel dùng chung
        channel = await connection.createChannel();
        
        // Đảm bảo queue tồn tại ngay khi khởi tạo
        await channel.assertQueue(QUEUE_NAME, { durable: true });

        logger.info('✅ [RabbitMQ] Đã kết nối và tạo Channel thành công');
        isConnecting = false;

    } catch (error) {
        logger.error({ err: error.message }, '❌ [RabbitMQ] Lỗi kết nối. Thử lại sau 5 giây...');
        isConnecting = false;
        setTimeout(() => connectRabbitMQ(logger), 5000);
    }
}

/**
 * Hàm gửi message vào queue (Sử dụng channel dùng chung)
 */
async function sendToQueue(data, requestId, logger) {
    try {
        // Đảm bảo channel đã sẵn sàng
        if (!channel) {
            if (logger) logger.warn('⚠️ [RabbitMQ] Channel chưa sẵn sàng, đang thử kết nối lại...');
            await connectRabbitMQ(logger);
            
            if (!channel) throw new Error('RabbitMQ channel is not available');
        }

        const payload = JSON.stringify({ ...data, requestId });
        
        // persistent: true để message được ghi xuống đĩa, chống mất mát dữ liệu
        const sent = channel.sendToQueue(QUEUE_NAME, Buffer.from(payload), { persistent: true });

        if (!sent) {
            throw new Error('QUEUE_BACKPRESSURE');
        }

        if (logger) logger.info({ requestId, action: data.action }, `[x] Da gui Job xu ly file: ${data.title}`);
        return sent;
    } catch (error) {
        if (logger) logger.error({ requestId, err: error }, 'Loi gui tin nhan den RabbitMQ');
        throw error;
    }
}

/**
 * Đóng kết nối gọn gàng (Thường dùng khi tắt app graceful shutdown)
 */
async function closeConnection(logger = console) {
    try {
        if (channel) await channel.close();
        if (connection) await connection.close();
        logger.info('🛑 [RabbitMQ] Đã đóng kết nối an toàn.');
    } catch (error) {
        logger.error({ err: error }, 'Lỗi khi đóng RabbitMQ');
    }
}

module.exports = { 
    connectRabbitMQ, 
    sendToQueue, 
    closeConnection, 
    QUEUE_NAME 
};