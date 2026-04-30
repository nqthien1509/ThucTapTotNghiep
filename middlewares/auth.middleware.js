const admin = require('firebase-admin');

const verifyToken = async (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ message: 'Unauthorized: Thiếu hoặc sai định dạng token!' });
    }
    
    try {
        // Tách token từ chuỗi "Bearer <token>"
        const token = authHeader.split(' ')[1];
        const decodedToken = await admin.auth().verifyIdToken(token);
        
        // Gắn thông tin user vào request để các controller sử dụng
        req.user = decodedToken; 
        next();
    } catch (error) {
        req.log.warn({ err: error }, 'Lỗi xác thực Firebase Token');
        return res.status(401).json({ message: 'Unauthorized: Token không hợp lệ!' });
    }
};

const optionalVerifyToken = async (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
        try {
            const token = authHeader.split(' ')[1];
            req.user = await admin.auth().verifyIdToken(token);
        } catch (error) { 
            req.log.debug('Token không hợp lệ, truy cập ẩn danh.'); 
        }
    }
    next(); 
};

module.exports = { verifyToken, optionalVerifyToken };