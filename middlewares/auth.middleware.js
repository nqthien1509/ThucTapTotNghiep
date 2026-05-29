const admin = require('firebase-admin');
const User = require('../models/User'); 

// 1. Middleware bắt buộc phải đăng nhập (Xác thực Firebase Token + Kiểm tra trạng thái DB)
const verifyToken = async (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ success: false, message: 'Unauthorized: Thiếu hoặc sai định dạng token!' });
    }
    
    try {
        const token = authHeader.split(' ')[1];
        const decodedToken = await admin.auth().verifyIdToken(token);
        
        // Lấy dữ liệu thực tế từ DB, vô hiệu hóa Mongoose Strict Mode
        let dbUser = await User.findById(decodedToken.uid).lean();

        // Fallback: Đề phòng trường hợp UID lưu ở trường khác
        if (!dbUser) {
            dbUser = await User.findOne({ 
                $or: [{ uid: decodedToken.uid }, { firebaseUid: decodedToken.uid }] 
            }).lean();
        }

        if (!dbUser) {
            return res.status(404).json({ success: false, message: 'Tài khoản không tồn tại trên hệ thống dữ liệu!' });
        }

        // CHỐNG CÁC THÀNH PHẦN KHIẾM NHÃ
        if (dbUser.isBlocked) {
            return res.status(403).json({ 
                success: false, 
                message: 'Tài khoản của bạn đã bị khóa do vi phạm tiêu chuẩn cộng đồng hoặc sử dụng ngôn từ khiếm nhã!' 
            });
        }
        
        // Gắn thông tin Firebase và thông tin DB đầy đủ vào req.user
        req.user = {
            ...decodedToken,
            role: dbUser.role || 'student', 
            isBlocked: dbUser.isBlocked || false
        }; 
        
        next();
    } catch (error) {
        console.error('LỖI XÁC THỰC FIREBASE TOKEN:', error.message);
        return res.status(401).json({ success: false, message: 'Unauthorized: Token hết hạn hoặc không hợp lệ!' });
    }
};

// 2. Middleware kiểm tra phân quyền Admin 
const isAdmin = async (req, res, next) => {
    if (!req.user) {
        return res.status(401).json({ success: false, message: 'Unauthorized: Yêu cầu xác thực tài khoản trước!' });
    }

    // =======================================================
    // 🌟 THẺ VIP TỐI THƯỢNG 
    // Ép cứng quyền Admin cho tài khoản của bạn, BỎ QUA kiểm tra role trong Database!
    // =======================================================
    if (req.user.uid === 'Tr1v7ODBt4V567WqoDbHmAHviol1' || req.user.email === '1@gmail.com') {
        console.log('👑 BOSSS ĐÃ VÀO HỆ THỐNG - CẤP QUYỀN ADMIN NGAY LẬP TỨC!');
        return next(); // Mở cổng cho đi qua luôn!
    }

    // Với các User khác thì hệ thống vẫn kiểm tra khắt khe bình thường
    if (req.user.role !== 'admin') {
        return res.status(403).json({ 
            success: false, 
            message: 'Quyền truy cập bị từ chối: Chỉ dành cho Quản trị viên (Admin)!' 
        });
    }

    next();
};

// 3. Middleware không bắt buộc đăng nhập (Dùng cho xem danh sách chung)
const optionalVerifyToken = async (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
        try {
            const token = authHeader.split(' ')[1];
            const decodedToken = await admin.auth().verifyIdToken(token);
            
            let dbUser = await User.findById(decodedToken.uid).lean();
            if (!dbUser) {
                dbUser = await User.findOne({ 
                    $or: [{ uid: decodedToken.uid }, { firebaseUid: decodedToken.uid }] 
                }).lean();
            }

            if (dbUser && !dbUser.isBlocked) {
                req.user = {
                    ...decodedToken,
                    role: dbUser.role || 'student',
                    isBlocked: false
                };
            }
        } catch (error) { 
            console.log('Token không hợp lệ hoặc hết hạn, tiếp tục truy cập dưới dạng ẩn danh.'); 
        }
    }
    next(); 
};

module.exports = { verifyToken, isAdmin, optionalVerifyToken };