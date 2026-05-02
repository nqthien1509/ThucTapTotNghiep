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
// [CẬP NHẬT]: Phải gọi .single('avatar') để Multer trả về một hàm middleware hợp lệ.
// 'avatar' chính là tên cái field (key) mà Frontend/Mobile sẽ gửi kèm file lên.
router.post('/:uid/avatar', verifyToken, uploadImage.single('avatar'), userController.updateAvatar);

module.exports = router;