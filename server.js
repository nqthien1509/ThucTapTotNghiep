const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const multer = require('multer');
const path = require('path');
const fs = require('fs'); 
const Document = require('./models/Document'); 

const app = express();

app.use(cors());
app.use(express.json()); 
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Kết nối MongoDB
mongoose.connect('mongodb://127.0.0.1:27017/thuctaptotnghiep_db')
    .then(() => console.log('✅ Đã kết nối thành công với MongoDB!'))
    .catch((err) => console.error('❌ Lỗi kết nối MongoDB:', err));

// =======================================================
// --- TỰ ĐỘNG TẠO THƯ MỤC UPLOADS ---
// =======================================================
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir);
    console.log('📁 Đã tự động tạo thư mục "uploads" để chứa file!');
}

// --- CẤU HÌNH "LỄ TÂN" MULTER ---
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'uploads/'); 
    },
    filename: function (req, file, cb) {
        cb(null, Date.now() + '-' + file.originalname);
    }
});
const upload = multer({ storage: storage });

// =======================================================
// API 1: NHẬN FILE VÀ LƯU THÔNG TIN (UPLOAD)
// =======================================================
app.post('/api/upload', upload.single('file'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ message: 'Vui lòng chọn một file PDF!' });
        }

        const { title, authorName } = req.body;
        const sizeInMB = (req.file.size / (1024 * 1024)).toFixed(2) + ' MB';

        const newDoc = new Document({
            title: title || 'Tài liệu không tên',
            authorName: authorName || 'Người dùng Ẩn danh', 
            fileUrl: '/uploads/' + req.file.filename,       
            size: sizeInMB
        });

        await newDoc.save();

        res.status(200).json({ 
            message: 'Tải tài liệu lên thành công!', 
            document: newDoc 
        });

    } catch (error) {
        console.error('Lỗi khi upload:', error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// =======================================================
// API 2: LẤY DANH SÁCH TÀI LIỆU (GET) - THÊM MỚI Ở ĐÂY 👇
// =======================================================
app.get('/api/documents', async (req, res) => {
    try {
        // Tìm tất cả tài liệu trong Database, sắp xếp theo ngày đăng (Mới nhất lên đầu: -1)
        const documents = await Document.find().sort({ uploadDate: -1 });
        
        // Trả danh sách về cho Android dưới dạng JSON
        res.status(200).json(documents);
    } catch (error) {
        console.error('Lỗi khi lấy danh sách:', error);
        res.status(500).json({ message: 'Lỗi Server khi lấy dữ liệu!' });
    }
});
// =======================================================
// =======================================================
// API 3: LẤY CHI TIẾT 1 TÀI LIỆU THEO ID
// =======================================================
app.get('/api/documents/:id', async (req, res) => {
    try {
        // Dùng hàm findById của Mongoose để mò tìm đúng cuốn sách
        const doc = await Document.findById(req.params.id);
        if (!doc) {
            return res.status(404).json({ message: 'Không tìm thấy tài liệu!' });
        }
        res.status(200).json(doc);
    } catch (error) {
        console.error('Lỗi khi lấy chi tiết tài liệu:', error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});
// =======================================================
// API 4: TÌM KIẾM TÀI LIỆU THEO TỪ KHÓA
// =======================================================
app.get('/api/search', async (req, res) => {
    try {
        // Lấy từ khóa do Android gửi lên (Ví dụ: ?q=android)
        const keyword = req.query.q;
        
        if (!keyword) {
            // Nếu không gõ gì thì trả về danh sách rỗng
            return res.status(200).json([]);
        }

        // Tìm kiếm trong MongoDB: Dùng $regex để tìm chuỗi chứa từ khóa, $options: 'i' để không phân biệt HOA/thường
        const documents = await Document.find({
            title: { $regex: keyword, $options: 'i' }
        }).sort({ uploadDate: -1 }); // Vẫn sắp xếp mới nhất lên đầu
        
        res.status(200).json(documents);
    } catch (error) {
        console.error('Lỗi khi tìm kiếm:', error);
        res.status(500).json({ message: 'Lỗi Server khi tìm kiếm!' });
    }
});
// =======================================================
// API 5: LẤY DANH SÁCH TÀI LIỆU CỦA RIÊNG TÔI
// =======================================================
app.get('/api/my-documents/:authorName', async (req, res) => {
    try {
        const author = req.params.authorName;
        // Tìm các sách có tác giả khớp với tên được truyền vào
        const documents = await Document.find({ authorName: author }).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        console.error('Lỗi khi lấy tài liệu cá nhân:', error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// =======================================================
// API 6: XÓA TÀI LIỆU (DATABASE & FILE VẬT LÝ)
// =======================================================
app.delete('/api/documents/:id', async (req, res) => {
    try {
        // 1. Tìm cuốn sách trong DB xem có tồn tại không
        const doc = await Document.findById(req.params.id);
        if (!doc) {
            return res.status(404).json({ message: 'Không tìm thấy tài liệu!' });
        }

        // 2. Tìm đường dẫn file PDF thật trong kho và xóa nó
        const fileName = path.basename(doc.fileUrl); // Lấy tên file (VD: 123-abc.pdf)
        const filePath = path.join(__dirname, 'uploads', fileName);
        if (fs.existsSync(filePath)) {
            fs.unlinkSync(filePath); // Tiêu hủy file
            console.log(`🗑 Đã xóa file vật lý: ${fileName}`);
        }

        // 3. Xóa bản ghi trong MongoDB
        await Document.findByIdAndDelete(req.params.id);
        
        res.status(200).json({ message: 'Xóa tài liệu thành công!' });
    } catch (error) {
        console.error('Lỗi khi xóa:', error);
        res.status(500).json({ message: 'Lỗi Server khi xóa tài liệu!' });
    }
});
app.get('/', (req, res) => {
    res.send('Xin chào! Backend của App Chia Sẻ Tài Liệu đã hoạt động! 🚀');
});

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`🚀 Server đang chạy tại http://localhost:${PORT}`);
});