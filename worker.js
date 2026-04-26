﻿require('dotenv').config();

const amqp = require('amqplib');
const mongoose = require('mongoose');
const Document = require('./models/Document');
const admin = require('firebase-admin');
const { QUEUE_NAME } = require('./services/rabbitmq.service');

const pino = require('pino');
const logger = pino({
    level: process.env.NODE_ENV === 'production' ? 'info' : 'debug',
    transport: process.env.NODE_ENV !== 'production' ? { target: 'pino-pretty' } : undefined
});

const serviceAccount = require('./firebase-key.json');
if (!admin.apps.length) {
    admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
}

const MONGO_URI = process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/thuctaptotnghiep_db';
const RABBITMQ_URL = process.env.RABBITMQ_URL;
const MAX_RETRIES = Number(process.env.WORKER_MAX_RETRIES || 3);

if (!RABBITMQ_URL) {
    logger.fatal('CRITICAL ERROR: Thieu bien moi truong RABBITMQ_URL');
    process.exit(1);
}

mongoose.connect(MONGO_URI)
    .then(() => logger.info('Worker da ket noi MongoDB thanh cong!'))
    .catch((err) => {
        logger.fatal({ err }, 'Worker loi ket noi MongoDB');
        process.exit(1);
    });

async function markDocumentFailed(documentId, requestId) {
    if (!documentId) return;

    try {
        await Document.findByIdAndUpdate(documentId, { status: 'failed' });
        logger.warn({ requestId, documentId }, 'Danh dau tai lieu failed sau khi vuot retry');
    } catch (err) {
        logger.error({ requestId, documentId, err }, 'Khong the cap nhat status failed');
    }
}

async function requeueWithRetry(channel, data) {
    const nextRetryCount = (Number(data.retryCount) || 0) + 1;
    const payload = { ...data, retryCount: nextRetryCount };

    channel.sendToQueue(QUEUE_NAME, Buffer.from(JSON.stringify(payload)), { persistent: true });
    return nextRetryCount;
}

async function processMessage(data) {
    const { documentId, title, userId, requestId } = data;

    logger.info({ requestId, documentId }, `[v] Nhan Job xu ly tai lieu: ${title}`);
    logger.debug({ requestId, documentId }, '--- Dang gia lap quet Virus va phan tich PDF ---');

    await new Promise((resolve) => setTimeout(resolve, 7000));

    const updatedDoc = await Document.findByIdAndUpdate(
        documentId,
        { status: 'verified' },
        { returnDocument: 'after' }
    );

    if (!updatedDoc) {
        throw new Error('DOCUMENT_NOT_FOUND');
    }

    logger.info({ requestId, documentId }, `[OK] Da xac thuc thanh cong tai lieu: ${title}`);

    if (!userId) {
        logger.warn({ requestId, documentId }, 'Khong co userId de gui push notification');
        return;
    }

    const topicName = `user_${String(userId).replace(/[^a-zA-Z0-9_\-]/g, '_')}`;
    const message = {
        notification: {
            title: 'Kiem duyet hoan tat!',
            body: `Tai lieu "${title}" cua ban da an toan va san sang de chia se.`
        },
        data: {
            documentId: String(documentId),
            action: 'OPEN_DOCUMENT_DETAIL'
        },
        topic: topicName
    };

    await admin.messaging().send(message);
    logger.info({ requestId, documentId, topicName }, 'Da gui thong bao thanh cong');
}

async function startWorker() {
    try {
        const connection = await amqp.connect(RABBITMQ_URL);
        const channel = await connection.createChannel();

        await channel.assertQueue(QUEUE_NAME, { durable: true });
        channel.prefetch(1);

        logger.info('Worker da san sang. Dang cho file moi...');

        channel.consume(QUEUE_NAME, async (msg) => {
            if (!msg) return;

            let data;
            try {
                data = JSON.parse(msg.content.toString());
            } catch (err) {
                logger.error({ err }, 'Message khong hop le JSON, bo qua');
                channel.ack(msg);
                return;
            }

            const requestId = data.requestId;
            const currentRetry = Number(data.retryCount) || 0;

            try {
                await processMessage(data);
                channel.ack(msg);
            } catch (err) {
                logger.error({ requestId, documentId: data.documentId, retry: currentRetry, err }, 'Loi xu ly job');

                if (currentRetry < MAX_RETRIES) {
                    const retriedTo = await requeueWithRetry(channel, data);
                    logger.warn({ requestId, documentId: data.documentId, retriedTo }, 'Da requeue job de thu lai');
                } else {
                    await markDocumentFailed(data.documentId, requestId);
                    logger.error({ requestId, documentId: data.documentId }, 'Job that bai sau so lan retry toi da');
                }

                channel.ack(msg);
            }
        });

        connection.on('error', (err) => {
            logger.error({ err }, 'Ket noi RabbitMQ gap loi');
        });

        connection.on('close', () => {
            logger.warn('Ket noi RabbitMQ dong. Thu ket noi lai sau 5 giay...');
            setTimeout(startWorker, 5000);
        });
    } catch (error) {
        logger.error({ err: error }, 'Loi ket noi RabbitMQ. Dang thu lai...');
        setTimeout(startWorker, 5000);
    }
}

startWorker();