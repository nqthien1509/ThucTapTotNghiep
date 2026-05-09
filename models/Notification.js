const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
    userId: { type: String, required: true, index: true }, // Thêm index để lấy thông báo nhanh hơn
    title: { type: String, required: true },
    body: { type: String, required: true },
    data: { type: Object, default: {} }, // Lưu thêm documentId, type...
    isRead: { type: Boolean, default: false }, 
    createdAt: { 
        type: Date, 
        default: Date.now, 
        expires: 2592000 // Tự động xóa sau 30 ngày (2592000 giây)
    }
});

notificationSchema.index({ userId: 1, createdAt: -1 });

module.exports = mongoose.model('Notification', notificationSchema);