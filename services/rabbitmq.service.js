﻿const amqp = require('amqplib');

const QUEUE_NAME = 'file_processing_queue';

async function sendToQueue(data, requestId, logger) {
    let connection;
    let channel;

    try {
        connection = await amqp.connect(process.env.RABBITMQ_URL);
        channel = await connection.createChannel();
        await channel.assertQueue(QUEUE_NAME, { durable: true });

        const payload = JSON.stringify({ ...data, requestId });
        const sent = channel.sendToQueue(QUEUE_NAME, Buffer.from(payload), { persistent: true });

        if (!sent) {
            throw new Error('QUEUE_BACKPRESSURE');
        }

        if (logger) logger.info({ requestId, action: data.action }, `[x] Da gui Job xu ly file: ${data.title}`);
    } catch (error) {
        if (logger) logger.error({ requestId, err: error }, 'Loi gui tin nhan den RabbitMQ');
        throw error;
    } finally {
        try { if (channel) await channel.close(); } catch (_) {}
        try { if (connection) await connection.close(); } catch (_) {}
    }
}

module.exports = { sendToQueue, QUEUE_NAME };