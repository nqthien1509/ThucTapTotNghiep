const Notification = require('../models/Notification');

class NotificationController {
    // Lấy danh sách thông báo của User hiện tại (Có phân trang)
    async getMyNotifications(req, res, next) {
        try {
            // Lấy page và limit từ query URL (mặc định trang 1, mỗi trang 20 items)
            const page = parseInt(req.query.page) || 1;
            const limit = parseInt(req.query.limit) || 20;
            const skip = (page - 1) * limit;

            const notifications = await Notification.find({ userId: req.user.uid })
                .sort({ createdAt: -1 }) 
                .skip(skip) // Bỏ qua các item của trang trước
                .limit(limit); // Chỉ lấy đủ số item của trang hiện tại
                
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

    // [MỚI] Đánh dấu MỘT thông báo là đã đọc (khi user nhấn vào xem)
    async markAsRead(req, res, next) {
        try {
            const { id } = req.params;
            // Dùng findOneAndUpdate với điều kiện userId để bảo mật (người khác không thể sửa thông báo của mình)
            await Notification.findOneAndUpdate(
                { _id: id, userId: req.user.uid }, 
                { $set: { isRead: true } }
            );
            res.status(200).json({ message: 'Đã đánh dấu đọc thông báo' });
        } catch (error) { next(error); }
    }

    // [MỚI] Xóa MỘT thông báo (khi user vuốt để xóa)
    async deleteNotification(req, res, next) {
        try {
            const { id } = req.params;
            // Tương tự, kiểm tra userId để đảm bảo bảo mật
            await Notification.findOneAndDelete({ _id: id, userId: req.user.uid }); 
            res.status(200).json({ message: 'Đã xóa thông báo' });
        } catch (error) { next(error); }
    }
}

module.exports = new NotificationController();