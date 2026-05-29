const express = require('express');
const router = express.Router();
const docController = require('../controllers/document.controller');
// [CẬP NHẬT]: Import thêm quyền isAdmin
const { verifyToken, optionalVerifyToken, isAdmin } = require('../middlewares/auth.middleware');
const { uploadPdf } = require('../middlewares/upload.middleware');
const { body } = require('express-validator');

// ============================================================
// [THÊM MỚI DÀNH CHO ADMIN]: QUẢN LÝ KHO TÀI LIỆU
// ============================================================
// Lấy danh sách toàn bộ tài liệu (bao gồm cả tài liệu đang chờ duyệt hoặc lỗi)
router.get('/admin/documents/all', verifyToken, isAdmin, docController.getAllDocumentsForAdmin);

// Xóa tài liệu trực tiếp, xóa luôn file vật lý và trừ điểm người đăng
router.delete('/admin/documents/:id', verifyToken, isAdmin, docController.deleteDocumentByAdmin);


// --- QUẢN LÝ TÀI LIỆU (Tải lên & Tìm kiếm) ---
router.post('/upload', verifyToken, uploadPdf.single('file'), [
    body('title').notEmpty().isLength({ max: 100 }),
    body('subject').notEmpty().isLength({ max: 50 }),
    body('category').isIn(['Slide', 'Đề thi', 'Giáo trình'])
], docController.upload);

router.get('/documents', docController.getAll);
router.get('/search', docController.search);
router.get('/my-documents', verifyToken, docController.getMyDocuments);

// ============================================================
// THÊM API LẤY BẢNG XẾP HẠNG TÀI LIỆU
// (Bắt buộc phải nằm TRƯỚC router.get('/documents/:id') )
// ============================================================
router.get('/documents/leaderboard/top', docController.getTopDocuments);


// --- CHI TIẾT & TƯƠNG TÁC TÀI LIỆU ---
router.get('/documents/:id', optionalVerifyToken, docController.getDetail);
router.delete('/documents/:id', verifyToken, docController.delete);

// Tương tác Favorite & Watch-later
router.post('/documents/:id/favorite', verifyToken, docController.toggleFavorite);
router.post('/documents/:id/watch-later', verifyToken, docController.toggleWatchLater);

// ============================================================
// API TĂNG LƯỢT XEM VÀ LƯỢT TẢI
// ============================================================
router.put('/documents/:id/view', docController.incrementView);
router.put('/documents/:id/download', docController.incrementDownload);

// --- NGƯỜI DÙNG ---
router.get('/users/:userId/favorites', verifyToken, docController.getFavorites);
router.get('/users/:userId/watch-later', verifyToken, docController.getWatchLater);

module.exports = router;