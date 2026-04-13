const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const amqp = require('amqplib'); // Thư viện kết nối RabbitMQ
const Document = require('./models/Document');
const User = require('./models/User'); // <-- MODEL USER

const app = express();

app.use(cors());
app.use(express.json());
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// =======================================================
// 1. KẾT NỐI DATABASE & RABBITMQ
// =======================================================
mongoose.connect('mongodb://127.0.0.1:27017/thuctaptotnghiep_db')
    .then(() => console.log('✅ Đã kết nối thành công với MongoDB!'))
    .catch((err) => console.error('❌ Lỗi kết nối MongoDB:', err));

// DÁN LINK CLOUDAMQP CỦA BẠN VÀO ĐÂY 👇
const RABBITMQ_URL = 'amqps://bkrezbsn:livqe0OmRbzLY_FxiDPV0n6nGmzooxO4@cougar.rmq.cloudamqp.com/bkrezbsn';

// Hàm gửi tin nhắn vào hàng đợi (Producer)
async function sendToQueue(data) {
    try {
        const connection = await amqp.connect(RABBITMQ_URL);
        const channel = await connection.createChannel();
        const queueName = 'file_processing_queue';

        // Đảm bảo hàng đợi tồn tại
        await channel.assertQueue(queueName, { durable: true });
        
        // Gửi dữ liệu dưới dạng Buffer
        channel.sendToQueue(queueName, Buffer.from(JSON.stringify(data)), {
            persistent: true // Tin nhắn sẽ không bị mất nếu server RabbitMQ khởi động lại
        });

        console.log(` [x] Đã gửi Job xử lý file: ${data.title}`);
        
        // Đóng kết nối sau khi gửi xong
        setTimeout(() => {
            connection.close();
        }, 500);
    } catch (error) {
        console.error('❌ Lỗi gửi tin nhắn đến RabbitMQ:', error);
    }
}

// =======================================================
// 2. CẤU HÌNH LƯU TRỮ FILE (MULTER)
// =======================================================
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir);
}

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, 'uploads/'),
    filename: (req, file, cb) => cb(null, Date.now() + '-' + file.originalname)
});
const upload = multer({ storage: storage });

// =======================================================
// 3. HỆ THỐNG API
// =======================================================

// ==========================================
// API UPLOAD TÀI LIỆU (PDF)
// ==========================================
app.post('/api/upload', upload.single('file'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ message: 'Vui lòng chọn một file PDF!' });
        }

        // 1. Lấy toàn bộ dữ liệu từ form
        const { title, authorName, subject, category, description, tags } = req.body;
        
        // 2. Xử lý Tags
        const tagsArray = tags ? tags.split(',').map(tag => tag.trim()).filter(tag => tag !== "") : [];
        const sizeInMB = (req.file.size / (1024 * 1024)).toFixed(2) + ' MB';

        // 3. Khởi tạo Document mới
        const newDoc = new Document({
            title: title || 'Tài liệu không tên',
            authorName: authorName || 'Người dùng Ẩn danh',
            subject: subject || 'Khác',
            category: category || 'Slide',
            description: description || '',
            tags: tagsArray,
            fileUrl: '/uploads/' + req.file.filename,
            size: sizeInMB,
            status: 'pending' 
        });

        await newDoc.save();

        sendToQueue({
            documentId: newDoc._id,
            title: newDoc.title,
            filePath: req.file.path,
            action: 'CHECK_VIRUS_AND_THUMBNAIL',
            authorName: newDoc.authorName 
        });

        res.status(200).json({
            message: 'Tài liệu đã được tải lên và đang chờ hệ thống xử lý ngầm!',
            document: newDoc
        });

    } catch (error) {
        console.error('Lỗi khi upload:', error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// =======================================================
// API: QUẢN LÝ THÔNG TIN NGƯỜI DÙNG (PROFILE)
// =======================================================

// 1. Lấy thông tin cá nhân
app.get('/api/user/:uid', async (req, res) => {
    try {
        let user = await User.findById(req.params.uid);
        
        // Nếu user chưa có trong MongoDB, trả về 1 object mặc định
        if (!user) {
            return res.status(200).json({
                _id: req.params.uid,
                email: "",
                displayName: "",
                school: "Trường Đại học Giao thông vận tải TP.HCM (UTH)",
                bio: "",
                avatarUrl: ""
            });
        }
        res.status(200).json(user);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Lỗi Server" });
    }
});

// 2. Cập nhật thông tin cá nhân (Tên, Trường, Bio)
app.put('/api/user/:uid', async (req, res) => {
    try {
        const { email, displayName, school, bio } = req.body;
        
        const updatedUser = await User.findByIdAndUpdate(
            req.params.uid,
            { email, displayName, school, bio },
            { new: true, upsert: true }
        );
        res.status(200).json(updatedUser);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Lỗi khi cập nhật profile" });
    }
});

// ==========================================
// CẬP NHẬT MỚI: API UPLOAD ẢNH ĐẠI DIỆN
// ==========================================
app.post('/api/user/:uid/avatar', upload.single('avatar'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ message: 'Vui lòng chọn một file ảnh!' });
        }

        // Tạo đường dẫn ảnh mới
        const newAvatarUrl = '/uploads/' + req.file.filename;

        // Cập nhật đường dẫn này vào MongoDB của User
        const updatedUser = await User.findByIdAndUpdate(
            req.params.uid,
            { avatarUrl: newAvatarUrl },
            { new: true, upsert: true }
        );

        res.status(200).json(updatedUser);
    } catch (error) {
        console.error('Lỗi khi upload avatar:', error);
        res.status(500).json({ message: 'Lỗi Server khi tải ảnh lên!' });
    }
});

// =======================================================
// API: TƯƠNG TÁC NGƯỜI DÙNG (YÊU THÍCH / XEM SAU)
// =======================================================

// 1. Toggle Yêu thích
app.post('/api/documents/:id/favorite', async (req, res) => {
    try {
        const { userId } = req.body; 
        if (!userId) return res.status(400).json({ message: 'Thiếu thông tin người dùng (userId)!' });

        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy tài liệu!' });

        const index = doc.favoritedBy.indexOf(userId);
        if (index === -1) {
            doc.favoritedBy.push(userId); // Lưu
        } else {
            doc.favoritedBy.splice(index, 1); // Bỏ lưu
        }

        await doc.save();
        res.status(200).json({ message: 'Cập nhật trạng thái yêu thích thành công!' });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// 2. Toggle Xem lại sau
app.post('/api/documents/:id/watch-later', async (req, res) => {
    try {
        const { userId } = req.body; 
        if (!userId) return res.status(400).json({ message: 'Thiếu thông tin người dùng (userId)!' });

        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy tài liệu!' });

        const index = doc.watchLaterBy.indexOf(userId);
        if (index === -1) {
            doc.watchLaterBy.push(userId); // Lưu
        } else {
            doc.watchLaterBy.splice(index, 1); // Bỏ lưu
        }

        await doc.save();
        res.status(200).json({ message: 'Cập nhật danh sách xem sau thành công!' });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// =======================================================
// API: TRUY XUẤT DỮ LIỆU
// =======================================================

// Lấy danh sách tất cả tài liệu
app.get('/api/documents', async (req, res) => {
    try {
        const documents = await Document.find().sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// LẤY CHI TIẾT TÀI LIỆU
app.get('/api/documents/:id', async (req, res) => {
    try {
        const { userId } = req.query; // Client cần gửi /api/documents/123?userId=...

        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy!' });

        const docData = doc.toObject();

        docData.isFavorite = userId ? doc.favoritedBy.includes(userId) : false;
        docData.isWatchLater = userId ? doc.watchLaterBy.includes(userId) : false;

        delete docData.favoritedBy;
        delete docData.watchLaterBy;

        res.status(200).json(docData);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// ==========================================
// API TÌM KIẾM CÓ THỂ LỌC THEO CATEGORY
// ==========================================
app.get('/api/search', async (req, res) => {
    try {
        const { q, category } = req.query;
        let queryObj = {};

        // Nếu có từ khóa tìm kiếm
        if (q) {
            queryObj.title = { $regex: q, $options: 'i' }; 
        }

        // Nếu có chọn Filter Chip (khác "Tất cả")
        if (category && category !== 'Tất cả') {
            queryObj.category = category; 
        }

        // Nếu không nhập chữ và không chọn filter nào (hoặc chọn "Tất cả") thì trả về rỗng
        if (!q && (!category || category === 'Tất cả')) {
            return res.status(200).json([]);
        }

        const documents = await Document.find(queryObj).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// Lấy tài liệu theo tác giả
app.get('/api/my-documents/:authorName', async (req, res) => {
    try {
        const documents = await Document.find({ authorName: req.params.authorName }).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// =======================================================
// API LẤY DANH SÁCH TÀI LIỆU ĐÃ LƯU CỦA USER
// =======================================================

// 1. Lấy danh sách tài liệu Yêu thích
app.get('/api/users/:userId/favorites', async (req, res) => {
    try {
        const { userId } = req.params;
        const documents = await Document.find({ favoritedBy: userId }).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// 2. Lấy danh sách tài liệu Xem lại sau
app.get('/api/users/:userId/watch-later', async (req, res) => {
    try {
        const { userId } = req.params;
        const documents = await Document.find({ watchLaterBy: userId }).sort({ uploadDate: -1 });
        res.status(200).json(documents);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

// =======================================================
// XÓA TÀI LIỆU
// =======================================================
app.delete('/api/documents/:id', async (req, res) => {
    try {
        const doc = await Document.findById(req.params.id);
        if (!doc) return res.status(404).json({ message: 'Không tìm thấy!' });
        
        const filePath = path.join(__dirname, 'uploads', path.basename(doc.fileUrl));
        if (fs.existsSync(filePath)) fs.unlinkSync(filePath);
        
        await Document.findByIdAndDelete(req.params.id);
        res.status(200).json({ message: 'Xóa thành công!' });
    } catch (error) {
        res.status(500).json({ message: 'Lỗi Server!' });
    }
});

app.get('/', (req, res) => res.send('Backend App Tài Liệu + RabbitMQ '));

const PORT = 3000;
app.listen(PORT, () => {
    console.log(` Server đang chạy tại http://localhost:${PORT}`);
});