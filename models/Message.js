const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema(
  {
    conversationId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Conversation',
      required: true,
    },
    senderId: {
      type: String, // [SỬA]: Đổi từ ObjectId thành String
      ref: 'User',
      required: true,
    },
    text: {
      type: String,
      required: true,
    },
    isRead: {
      type: Boolean,
      default: false, // Dùng để làm tính năng "Đã xem" hoặc hiển thị số tin nhắn chưa đọc sau này
    }
  },
  {
    timestamps: true, // createdAt chính là thời gian gửi tin nhắn
  }
);

module.exports = mongoose.model('Message', messageSchema);