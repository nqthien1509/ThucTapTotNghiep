const admin = require('firebase-admin');

const verifyToken = async (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ message: 'Unauthorized: Thiếu hoặc sai định dạng token!' });
    }
    try {
        const decodedToken = await admin.auth().verifyIdToken(authHeader.split(' ')[1]);
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
            req.user = await admin.auth().verifyIdToken(authHeader.split(' ')[1]);
        } catch (error) { req.log.debug('Token không hợp lệ, truy cập ẩn danh.'); }
    }
    next(); 
};

module.exports = { verifyToken, optionalVerifyToken };