const express = require('express');
const router = express.Router();
const categoryController = require('../controllers/category.controller');
const { verifyToken, isAdmin } = require('../middlewares/auth.middleware');

// Public API: App Mobile sẽ gọi cái này để lấy danh sách môn học hiển thị lên Dropdown
router.get('/', categoryController.getAllCategories);

// Admin API: Thêm và Xóa danh mục
router.post('/admin', verifyToken, isAdmin, categoryController.createCategory);
router.delete('/admin/:id', verifyToken, isAdmin, categoryController.deleteCategory);

module.exports = router;