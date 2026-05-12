const mongoose = require('mongoose');

const requestSchema = new mongoose.Schema(
  {
    author: {
      type: String, // [SỬA]: Đổi từ ObjectId thành String
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
      enum: ['open', 'resolved', 'closed'], // open: đang xin, resolved: đã có người cho, closed: đóng
      default: 'open',
    }
  },
  {
    timestamps: true, // Tự động tạo createdAt và updatedAt
  }
);

module.exports = mongoose.model('Request', requestSchema);