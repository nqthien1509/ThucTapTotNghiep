const express = require('express');
const router = express.Router();
const chatController = require('../controllers/chat.controller');
// [SỬA Ở ĐÂY]: Import đúng tên hàm verifyToken
const { verifyToken } = require('../middlewares/auth.middleware'); 

// [SỬA Ở ĐÂY]: Dùng verifyToken thay cho auth
router.post('/conversations', verifyToken, chatController.getOrCreateConversation);
router.get('/messages/:conversationId', verifyToken, chatController.getMessages);
// Thêm dòng này vào dưới các route cũ
router.get('/conversations/me', verifyToken, chatController.getConversations);
module.exports = router;