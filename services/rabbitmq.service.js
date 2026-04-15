const amqp = require('amqplib');

async function sendToQueue(data, requestId, logger) {
    try {
        const connection = await amqp.connect(process.env.RABBITMQ_URL);
        const channel = await connection.createChannel();
        await channel.assertQueue('file_processing_queue', { durable: true });
        
        channel.sendToQueue('file_processing_queue', Buffer.from(JSON.stringify({ ...data, requestId })), { persistent: true });
        
        if (logger) logger.info({ requestId, action: data.action }, `[x] Đã gửi Job xử lý file: ${data.title}`);
        setTimeout(() => connection.close(), 500);
    } catch (error) {
        if (logger) logger.error({ requestId, err: error }, ' Lỗi gửi tin nhắn đến RabbitMQ');
    }
}

module.exports = { sendToQueue };