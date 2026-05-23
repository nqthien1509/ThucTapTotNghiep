const mongoose = require('mongoose');

const documentSchema = new mongoose.Schema(
  {
    // Thông tin người đăng
    userId: { type: String, required: true, index: true }, 

    // Thông tin cơ bản của tài liệu
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
    
    // Nơi lưu trữ file
    fileUrl: { type: String, required: true },
    thumbnailUrl: { type: String, default: null }, 
    size: { type: String }, 
    
    // Trạng thái (Kiểm duyệt)
    status: { type: String, default: 'pending', enum: ['pending', 'verified', 'failed'] },
    
    // ==========================================
    // [CẬP NHẬT TRỌNG TÂM CHO BẢNG XẾP HẠNG]
    // ==========================================
    
    // Đánh index: true để API lấy bảng xếp hạng chạy siêu tốc
    views: { type: Number, default: 0, index: true },
    downloads: { type: Number, default: 0, index: true },
    
    // [THÊM MỚI] - Chuẩn bị cho chức năng Đánh giá (Rating 1-5 sao)
    averageRating: { type: Number, default: 0 },
    totalRatings: { type: Number, default: 0 },
    
    // ==========================================
    
    // Mảng lưu danh sách UID của những người đã tương tác
    favoritedBy: { type: [String], default: [], index: true },
    watchLaterBy: { type: [String], default: [], index: true },
    
    // (Vẫn giữ uploadDate để tương thích với code cũ của bạn nếu có)
    uploadDate: { type: Date, default: Date.now },
  },
  {
    // Tự động sinh ra createdAt và updatedAt
    timestamps: true,
  }
);

module.exports = mongoose.model('Document', documentSchema);