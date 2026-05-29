const express = require('express');
const router = express.Router();
const userController = require('../controllers/user.controller');
// [CẬP NHẬT]: Import thêm quyền isAdmin
const { verifyToken, isAdmin } = require('../middlewares/auth.middleware');
const { uploadImage } = require('../middlewares/upload.middleware'); 

// ==========================================
// ĐỊNH TUYẾN API CHO NGƯỜI DÙNG (USER MODULE)
// ==========================================

// =========================================================================
// [THÊM MỚI DÀNH CHO ADMIN] - QUẢN LÝ NGƯỜI DÙNG
// LƯU Ý: Phải đặt TRƯỚC route '/:uid' để tránh lỗi Express nhận nhầm param
// =========================================================================
// 5. Lấy danh sách toàn bộ người dùng
router.get('/admin/users', verifyToken, isAdmin, userController.getAllUsers);

// 6. Khóa / Mở khóa tài khoản thủ công
router.put('/admin/users/:id/toggle-block', verifyToken, isAdmin, userController.toggleBlockUser);
// =========================================================================


// =========================================================================
// CÁC TÍNH NĂNG NÂNG CẤP (BẢNG XẾP HẠNG)
// =========================================================================
// 4. Lấy bảng xếp hạng Top 10 người dùng đóng góp nhiều nhất
router.get('/leaderboard/top', verifyToken, userController.getTopContributors);


// =========================================================================
// API DÀNH CHO CÁ NHÂN (USER PROFILE)
// =========================================================================
// 1. Lấy thông tin cá nhân của User
router.get('/:uid', verifyToken, userController.getProfile);

// 2. Cập nhật thông tin chữ (Tên, Trường, Bio...)
router.put('/:uid', verifyToken, userController.updateProfile);

// 3. Cập nhật ảnh đại diện (Avatar)
router.post('/:uid/avatar', verifyToken, uploadImage.single('avatar'), userController.updateAvatar);

module.exports = router;