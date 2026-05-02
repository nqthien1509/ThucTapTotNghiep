const Notification = require('../models/Notification');

class NotificationController {
    // Lấy danh sách thông báo của User hiện tại
    async getMyNotifications(req, res, next) {
        try {
            const notifications = await Notification.find({ userId: req.user.uid })
                .sort({ createdAt: -1 }) // Mới nhất lên đầu
                .limit(50); // Giới hạn 50 cái gần nhất
            res.status(200).json(notifications);
        } catch (error) { next(error); }
    }

    // Đánh dấu tất cả là đã đọc
    async markAllAsRead(req, res, next) {
        try {
            await Notification.updateMany(
                { userId: req.user.uid, isRead: false },
                { $set: { isRead: true } }
            );
            res.status(200).json({ message: 'Đã đánh dấu đọc tất cả' });
        } catch (error) { next(error); }
    }
}

module.exports = new NotificationController();