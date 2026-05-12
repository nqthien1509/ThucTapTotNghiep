const mongoose = require('mongoose');

const conversationSchema = new mongoose.Schema(
  {
    requestId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Request', // Biết được phòng chat này bắt nguồn từ bài đăng yêu cầu nào
      required: true,
    },
    participants: [
      {
        type: String, // [SỬA]: Đổi từ ObjectId thành String
        ref: 'User',
        required: true,
      }
    ],
    lastMessage: {
      type: String,
      default: "", // Lưu trước đoạn tin nhắn cuối cùng để hiển thị ở danh sách chat (tránh phải query bảng Message nhiều)
    }
  },
  {
    timestamps: true,
  }
);

module.exports = mongoose.model('Conversation', conversationSchema);