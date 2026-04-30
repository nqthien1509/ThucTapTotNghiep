const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Đảm bảo thư mục uploads luôn tồn tại
const uploadDir = path.join(__dirname, '../uploads');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir);

// 1. Cấu hình nơi lưu và tên file
const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, 'uploads/'),
    filename: (req, file, cb) => {
        const safeName = file.originalname.replace(/[^a-zA-Z0-9.\-_]/g, '_');
        cb(null, Date.now() + '-' + safeName);
    }
});

// 2. TẠO BỘ LỌC ĐỘC LẬP
// Bộ lọc CHỈ cho phép PDF
const pdfFilter = (req, file, cb) => {
    if (file.mimetype === 'application/pdf') {
        cb(null, true);
    } else {
        cb(new Error('INVALID_FILE_TYPE: Chỉ cho phép định dạng PDF!'), false);
    }
};

// Bộ lọc CHỈ cho phép Hình ảnh
const imageFilter = (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
        cb(null, true);
    } else {
        cb(new Error('INVALID_FILE_TYPE: Chỉ cho phép định dạng hình ảnh!'), false);
    }
};

// 3. KHỞI TẠO 2 INSTANCE MULTER RIÊNG BIỆT
// Dung lượng tối đa: PDF 25MB
const uploadPdf = multer({ 
    storage: storage, 
    fileFilter: pdfFilter,
    limits: { fileSize: 25 * 1024 * 1024 } 
});

// Dung lượng tối đa: Ảnh 5MB
const uploadImage = multer({ 
    storage: storage, 
    fileFilter: imageFilter,
    limits: { fileSize: 5 * 1024 * 1024 } 
});

// 4. EXPORT CÁC MIDDLEWARE VÀ FILTER
module.exports = {
    uploadPdf,
    uploadImage,
    pdfFilter,  // Export hàm này để phục vụ Unit Test
    imageFilter // Export thêm hàm này dự phòng cho Unit Test ảnh sau này
};