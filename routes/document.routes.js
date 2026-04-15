const express = require('express');
const router = express.Router();
const docController = require('../controllers/document.controller');
const { verifyToken, optionalVerifyToken } = require('../middlewares/auth.middleware');
const { uploadPdf } = require('../middlewares/upload.middleware');
const { body } = require('express-validator');

router.post('/upload', verifyToken, uploadPdf, [
    body('title').notEmpty().isLength({ max: 100 }),
    body('subject').notEmpty().isLength({ max: 50 }),
    body('category').isIn(['Slide', 'Đề thi', 'Giáo trình'])
], docController.upload);

router.post('/documents/:id/favorite', verifyToken, docController.toggleFavorite);
router.post('/documents/:id/watch-later', verifyToken, docController.toggleWatchLater);
router.get('/documents', docController.getAll);
router.get('/documents/:id', optionalVerifyToken, docController.getDetail);
router.get('/search', docController.search);
router.get('/my-documents', verifyToken, docController.getMyDocuments);
router.get('/users/:userId/favorites', verifyToken, docController.getFavorites);
router.get('/users/:userId/watch-later', verifyToken, docController.getWatchLater);
router.delete('/documents/:id', verifyToken, docController.delete);

module.exports = router;