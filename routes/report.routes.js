const express = require('express');
const router = express.Router();
const admin = require('firebase-admin'); // [THÊM MỚI]: Import Firebase Admin để bắn thông báo
const reportController = require('../controllers/report.controller');
const { verifyToken, isAdmin } = require('../middlewares/auth.middleware');

// =======================================================
// Import các Model để đếm số liệu thống kê
// =======================================================
const User = require('../models/User');
const Document = require('../models/Document');
const Report = require('../models/Report');

// 1. Tuyến đường cho User gửi đơn tố cáo ngôn từ khiếm nhã / tài liệu xấu
router.post('/', verifyToken, reportController.createReport);

// 2. Tuyến đường riêng biệt dành cho quản trị viên (Admin Dashboard)
router.get('/admin/all', verifyToken, isAdmin, reportController.getAllReports);
router.put('/admin/action/:id', verifyToken, isAdmin, reportController.actionReport);

// =======================================================
// 3. API lấy số liệu vẽ Biểu đồ cho Dashboard
// =======================================================
router.get('/admin/dashboard-stats', verifyToken, isAdmin, async (req, res) => {
    try {
        // 1. Lấy các số liệu tổng
        const totalUsers = await User.countDocuments();
        const totalDocuments = await Document.countDocuments();
        const pendingReports = await Report.countDocuments({ status: 'pending' });

        // 2. Thống kê số lượng tài liệu theo Trường học (Top 5)
        const documentsBySchool = await Document.aggregate([
            {
                $group: {
                    _id: "$school", // Nhóm theo tên trường
                    count: { $sum: 1 } 
                }
            },
            { $sort: { count: -1 } }, 
            { $limit: 5 } 
        ]);

        // 3. Thống kê tỷ lệ loại Báo cáo (Tài liệu vs Người dùng)
        const reportsByType = await Report.aggregate([
            {
                $group: {
                    _id: "$type", 
                    count: { $sum: 1 }
                }
            }
        ]);

        // Trả dữ liệu về cho Web Admin
        return res.status(200).json({
            success: true,
            data: {
                summary: {
                    totalUsers,
                    totalDocuments,
                    pendingReports
                },
                charts: {
                    documentsBySchool,
                    reportsByType
                }
            }
        });
    } catch (error) {
        console.error('LỖI LẤY STATS DASHBOARD:', error.message);
        return res.status(500).json({ success: false, message: 'Lỗi server khi lấy số liệu thống kê!' });
    }
});

// =======================================================
// [THÊM MỚI]: 4. API Bắn thông báo toàn hệ thống (Broadcast)
// =======================================================
router.post('/admin/broadcast', verifyToken, isAdmin, async (req, res) => {
    const { title, body } = req.body;

    if (!title || !body) {
        return res.status(400).json({ success: false, message: 'Vui lòng nhập đủ Tiêu đề và Nội dung!' });
    }

    try {
        // Cấu hình gói tin gửi đi
        const message = {
            topic: 'all_users', // Gửi thẳng vào kênh mà toàn bộ App đã đăng ký
            notification: {
                title: title,
                body: body
            },
            android: {
                priority: 'high', // Ép điện thoại kêu Ting Ting ngay lập tức
                notification: {
                    sound: 'default'
                }
            }
        };

        // Bóp cò gửi đi thông qua Firebase Admin
        const response = await admin.messaging().send(message);
        console.log('🚀 ĐÃ BẮN THÔNG BÁO BROADCAST:', response);

        return res.status(200).json({ success: true, message: 'Đã bắn thông báo thành công tới tất cả thiết bị!' });
    } catch (error) {
        console.error('LỖI GỬI THÔNG BÁO:', error);
        return res.status(500).json({ success: false, message: 'Lỗi khi gọi Firebase Cloud Messaging!' });
    }
});

module.exports = router;