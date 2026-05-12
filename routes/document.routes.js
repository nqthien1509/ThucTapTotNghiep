const express = require('express');
const router = express.Router();
const docController = require('../controllers/document.controller');
const { verifyToken, optionalVerifyToken } = require('../middlewares/auth.middleware');
const { uploadPdf } = require('../middlewares/upload.middleware');
const { body } = require('express-validator');

// --- QUẢN LÝ TÀI LIỆU (Tải lên & Tìm kiếm) ---
router.post('/upload', verifyToken, uploadPdf.single('file'), [
    body('title').notEmpty().isLength({ max: 100 }),
    body('subject').notEmpty().isLength({ max: 50 }),
    body('category').isIn(['Slide', 'Đề thi', 'Giáo trình'])
], docController.upload);

router.get('/documents', docController.getAll);
router.get('/search', docController.search);
router.get('/my-documents', verifyToken, docController.getMyDocuments);

// --- CHI TIẾT & TƯƠNG TÁC TÀI LIỆU ---
router.get('/documents/:id', optionalVerifyToken, docController.getDetail);
router.delete('/documents/:id', verifyToken, docController.delete);

// [MỚI]: Tương tác Favorite & Watch-later
router.post('/documents/:id/favorite', verifyToken, docController.toggleFavorite);
router.post('/documents/:id/watch-later', verifyToken, docController.toggleWatchLater);

// ============================================================
// [QUAN TRỌNG]: API TĂNG LƯỢT XEM VÀ LƯỢT TẢI (Fix lỗi 404)
// ============================================================
// Sử dụng .put để khớp với @PUT của Android Retrofit
router.put('/documents/:id/view', docController.incrementView);
router.put('/documents/:id/download', docController.incrementDownload);

// --- NGƯỜI DÙNG ---
router.get('/users/:userId/favorites', verifyToken, docController.getFavorites);
router.get('/users/:userId/watch-later', verifyToken, docController.getWatchLater);

module.exports = router;