const mongoose = require('mongoose');

// ==========================================
// [THÊM MỚI]: Cấu trúc của một Bình luận
// ==========================================
const commentSchema = new mongoose.Schema({
  user: { 
    type: String, 
    ref: 'User', 
    required: true 
  }, // Lưu UID của người bình luận
  content: { 
    type: String, 
    required: [true, 'Nội dung bình luận không được để trống'] 
  },
  createdAt: { 
    type: Date, 
    default: Date.now 
  }
});

const requestSchema = new mongoose.Schema(
  {
    author: {
      type: String, // Trùng khớp với UID của Firebase
      ref: 'User',
      required: true,
    },
    title: {
      type: String,
      required: [true, 'Vui lòng nhập tiêu đề yêu cầu'],
      trim: true,
    },
    description: {
      type: String,
      required: [true, 'Vui lòng nhập mô tả chi tiết'],
      trim: true,
    },
    status: {
      type: String,
      enum: ['open', 'resolved', 'closed'], 
      default: 'open',
    },
    // Danh sách người dùng (UID) đã upvote
    upvotes: [{
      type: String,
      ref: 'User'
    }],
    // Link tài liệu khi có người tìm thấy
    resolvedLink: {
      type: String,
      default: null
    },
    // Người đã cung cấp tài liệu đó
    resolvedBy: {
      type: String,
      ref: 'User',
      default: null
    },
    // ==========================================
    // [THÊM MỚI]: Mảng chứa danh sách các bình luận thảo luận
    // ==========================================
    comments: [commentSchema]
  },
  {
    timestamps: true, // Tự động tạo createdAt và updatedAt
  }
);

module.exports = mongoose.model('Request', requestSchema);