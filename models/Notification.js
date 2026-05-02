const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
    userId: { type: String, required: true }, // Người nhận thông báo
    title: { type: String, required: true },
    body: { type: String, required: true },
    data: { type: Object, default: {} }, // Lưu thêm documentId, type...
    isRead: { type: Boolean, default: false }, // Trạng thái đã đọc/chưa đọc
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Notification', notificationSchema);