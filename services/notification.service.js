const admin = require('firebase-admin');
const userRepo = require('../repositories/user.repository');
const Notification = require('../models/Notification'); // [MỚI] Import Model Notification để lưu vào DB

function topicFromUid(uid) {
    return `user_${String(uid).replace(/[^a-zA-Z0-9_\-]/g, '_')}`;
}

/**
 * Gửi thông báo đến một user cụ thể thông qua Topic VÀ lưu vào Database
 */
async function sendNotificationToUser(targetUid, title, body, data = {}) {
    try {
        // 1. [MỚI] Lưu thông báo vào MongoDB để hiển thị ở NotificationScreen
        await Notification.create({
            userId: targetUid,
            title: title,
            body: body,
            data: data
        });

        // 2. Định tuyến Topic cho Firebase Cloud Messaging
        const topic = topicFromUid(targetUid);
        const message = {
            notification: {
                title: title,
                body: body
            },
            data: data, 
            topic: topic
        };

        // 3. Thực hiện bắn Push Notification
        const response = await admin.messaging().send(message);
        console.log(` [FCM] Đã lưu DB và gửi Push Notification tới ${topic}:`, response);
    } catch (error) {
        console.error(` [FCM] Lỗi xử lý thông báo:`, error);
    }
}

/**
 * Logic riêng cho sự kiện Like (Thả tim)
 */
async function notifyDocumentFavorited({ ownerUid, likerUid, documentId, documentTitle, logger }) {
    // Không tự gửi thông báo cho chính mình
    if (!ownerUid || !likerUid || String(ownerUid) === String(likerUid)) {
        return;
    }

    const liker = await userRepo.findByUid(likerUid);
    const likerName = liker?.displayName?.trim() || 'Một người dùng';
    const safeTitle = documentTitle || 'tài liệu của bạn';

    // Gọi hàm gửi thông báo dùng chung (Bây giờ nó sẽ tự động lưu vào DB luôn)
    await sendNotificationToUser(
        ownerUid,
        'Tài liệu đang hot! ',
        `${likerName} vừa thả tim "${safeTitle}".`,
        { 
            action: 'OPEN_DOCUMENT_DETAIL', 
            documentId: String(documentId),
            type: 'LIKE'
        }
    );

    if (logger) {
        logger.info({ ownerUid, likerUid, documentId }, 'Đã xử lý thông báo like tài liệu');
    }
}

module.exports = {
    sendNotificationToUser,
    notifyDocumentFavorited,
    topicFromUid
};