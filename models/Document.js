const mongoose = require('mongoose');

const documentSchema = new mongoose.Schema({
    // CẬP NHẬT QUAN TRỌNG: Thêm userId để lưu danh tính người đăng
    // Đánh index: true để tối ưu tốc độ load màn hình "Tài liệu của tôi"
    userId: { type: String, required: true, index: true }, 

    title: { type: String, required: true, trim: true },
    authorName: { type: String, required: true, trim: true },
    subject: { type: String, required: true }, 
    category: { 
        type: String, 
        enum: ['Slide', 'Đề thi', 'Giáo trình'], 
        required: true 
    }, 
    description: { type: String, trim: true }, 
    tags: { type: [String], default: [] }, 
    fileUrl: { type: String, required: true },
    size: { type: String }, // Nơi chứa dữ liệu dung lượng (VD: "17.50 MB")
    uploadDate: { type: Date, default: Date.now },
    status: { type: String, default: 'pending', enum: ['pending', 'verified', 'failed'] },
    
    // Mảng lưu danh sách UID của những người đã thả tim/xem sau
    favoritedBy: { type: [String], default: [], index: true },
    watchLaterBy: { type: [String], default: [], index: true }
});

module.exports = mongoose.model('Document', documentSchema);