const express = require('express');
const router = express.Router();
const notificationController = require('../controllers/notification.controller');
const { verifyToken } = require('../middlewares/auth.middleware');

// Lấy danh sách thông báo (có hỗ trợ phân trang qua query: ?page=1&limit=20)
router.get('/', verifyToken, notificationController.getMyNotifications);

// Đánh dấu tất cả là đã đọc
router.put('/read-all', verifyToken, notificationController.markAllAsRead);

// [MỚI] Đánh dấu MỘT thông báo cụ thể là đã đọc
router.put('/:id/read', verifyToken, notificationController.markAsRead);

// [MỚI] Xóa MỘT thông báo cụ thể
router.delete('/:id', verifyToken, notificationController.deleteNotification);

module.exports = router;