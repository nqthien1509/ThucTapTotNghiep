const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Đảm bảo thư mục uploads luôn tồn tại
const uploadDir = path.join(__dirname, '../uploads');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir);

// Cấu hình nơi lưu và tên file (đã sanitize để chống lỗi ký tự lạ)
const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, 'uploads/'),
    filename: (req, file, cb) => {
        const safeName = file.originalname.replace(/[^a-zA-Z0-9.\-_]/g, '_');
        cb(null, Date.now() + '-' + safeName);
    }
});

// Bộ lọc file chung: Chấp nhận PDF và tất cả các loại Ảnh
const upload = multer({ 
    storage: storage, 
    limits: { fileSize: 20 * 1024 * 1024 }, // Tối đa 20MB
    fileFilter: (req, file, cb) => {
        if (file.mimetype === 'application/pdf' || file.mimetype.startsWith('image/')) {
            cb(null, true);
        } else {
            cb(new Error('INVALID_TYPE'), false);
        }
    }
});

// ==========================================
// 1. MIDDLEWARE: UPLOAD TÀI LIỆU (PDF)
// Bắt file từ field name là 'file'
// ==========================================
const uploadPdf = (req, res, next) => {
    upload.single('file')(req, res, function (err) {
        if (err instanceof multer.MulterError) {
            // SỬA: 10MB thành 20MB để khớp với limits ở trên
            if (err.code === 'LIMIT_FILE_SIZE') return res.status(400).json({ message: 'File quá lớn! Tối đa 20MB.' });
            return res.status(400).json({ message: 'Lỗi upload: ' + err.message });
        } else if (err) {
            if (err.message === 'INVALID_TYPE') return res.status(400).json({ message: 'Chỉ cho phép tải lên file định dạng PDF hoặc Ảnh!' });
            return res.status(400).json({ message: err.message });
        }
        next();
    });
};

// ==========================================
// 2. MIDDLEWARE: UPLOAD ẢNH ĐẠI DIỆN (IMAGE)
// Bắt file từ field name là 'avatar'
// ==========================================
const uploadImage = (req, res, next) => {
    upload.single('avatar')(req, res, function (err) {
        if (err instanceof multer.MulterError) {
            // SỬA: 10MB thành 20MB
            if (err.code === 'LIMIT_FILE_SIZE') return res.status(400).json({ message: 'Ảnh quá lớn! Tối đa 20MB.' });
            return res.status(400).json({ message: 'Lỗi upload ảnh: ' + err.message });
        } else if (err) {
            if (err.message === 'INVALID_TYPE') return res.status(400).json({ message: 'Chỉ cho phép tải lên định dạng Ảnh!' });
            return res.status(400).json({ message: err.message });
        }
        next();
    });
};

module.exports = { uploadPdf, uploadImage };