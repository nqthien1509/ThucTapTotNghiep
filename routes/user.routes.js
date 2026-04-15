const express = require('express');
const router = express.Router();
const userController = require('../controllers/user.controller');
const { verifyToken } = require('../middlewares/auth.middleware');
const { uploadImage } = require('../middlewares/upload.middleware'); // Kéo middleware lọc ảnh vào

// ==========================================
// ĐỊNH TUYẾN API CHO NGƯỜI DÙNG (USER MODULE)
// Base URL: /api/user
// ==========================================

// 1. Lấy thông tin cá nhân của User
router.get('/:uid', verifyToken, userController.getProfile);

// 2. Cập nhật thông tin chữ (Tên, Trường, Bio...)
router.put('/:uid', verifyToken, userController.updateProfile);

// 3. Cập nhật ảnh đại diện (Avatar)
// Quy trình: Xác thực Token -> Lọc và lưu file ảnh -> Xử lý lưu đường dẫn vào DB
router.post('/:uid/avatar', verifyToken, uploadImage, userController.updateAvatar);

module.exports = router;