const express = require('express');
const router = express.Router();
const requestController = require('../controllers/request.controller');
const { verifyToken } = require('../middlewares/auth.middleware'); 

// 1. Lấy danh sách yêu cầu (Đã sắp xếp ưu tiên nhiều upvote)
router.get('/', verifyToken, requestController.getRequests);

// 2. Tạo một yêu cầu tài liệu mới
router.post('/', verifyToken, requestController.createRequest);

// ==========================================
// [THÊM MỚI] - 3. Lấy chi tiết yêu cầu kèm bình luận
// ==========================================
router.get('/:id', verifyToken, requestController.getRequestById);

// ==========================================
// CÁC TÍNH NĂNG TƯƠNG TÁC (Tương tác với bài viết)
// ==========================================

// 4. Upvote/Bỏ upvote một yêu cầu
// Tham số :id là ID của bài yêu cầu (Request ID)
router.post('/:id/upvote', verifyToken, requestController.upvoteRequest);

// 5. Giải quyết yêu cầu (Đóng topic bằng cách gửi link tài liệu)
// Cần gửi kèm body json: { "resolvedLink": "link_tai_lieu_o_day" }
router.post('/:id/resolve', verifyToken, requestController.resolveRequest);

// ==========================================
// [THÊM MỚI] - 6. Thêm bình luận vào diễn đàn thảo luận
// Cần gửi kèm body json: { "content": "Em chào anh, cho em xin tài liệu với ạ!" }
// ==========================================
router.post('/:id/comment', verifyToken, requestController.addComment);

// Export router ra để server.js có thể dùng được
module.exports = router;