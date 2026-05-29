const mongoose = require('mongoose');

const reportSchema = new mongoose.Schema(
  {
    // Người thực hiện báo cáo
    reporter: {
      type: String, // UID Firebase của người báo cáo
      ref: 'User',
      required: true,
    },
    // Loại báo cáo: report tài liệu hay report người dùng
    type: {
      type: String,
      enum: ['document', 'user'],
      required: true,
    },
    // Nếu type là 'document' -> Lưu ID của Document
    targetDocument: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Document',
      default: null,
    },
    // Nếu type là 'user' -> Lưu UID của người dùng bị báo cáo
    targetUser: {
      type: String,
      ref: 'User',
      default: null,
    },
    // Lý do báo cáo vi phạm
    reason: {
      type: String,
      required: [true, 'Vui lòng cung cấp lý do báo cáo vi phạm'],
      trim: true,
    },
    // Link hoặc bằng chứng đi kèm (ví dụ: ID của bài viết/yêu cầu nơi xảy ra bình luận thô tục)
    evidenceLink: {
      type: String,
      default: null,
    },
    // Trạng thái xử lý của Admin
    status: {
      type: String,
      enum: ['pending', 'resolved', 'dismissed'],
      default: 'pending',
    },
    // Ghi chú của Admin sau khi xử lý (ví dụ: "Đã khóa tài khoản 7 ngày")
    adminNotes: {
      type: String,
      default: '',
    },
  },
  {
    timestamps: true, // Tự động tạo ngày báo cáo và ngày cập nhật trạng thái
  }
);

module.exports = mongoose.model('Report', reportSchema);