const express = require('express');
const router = express.Router();
const requestController = require('../controllers/request.controller');
const { verifyToken } = require('../middlewares/auth.middleware'); 

// Gọi các hàm từ controller
router.get('/', verifyToken, requestController.getRequests);
router.post('/', verifyToken, requestController.createRequest);

// Export router ra để server.js có thể dùng được
module.exports = router;