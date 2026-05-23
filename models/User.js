﻿const mongoose = require('mongoose');

const userSchema = new mongoose.Schema(
  {
    _id: { type: String, required: true }, // UID từ Firebase
    email: { type: String, default: '' },
    displayName: { type: String, default: 'Người dùng ẩn danh' },
    avatarUrl: { type: String, default: '' },
    
    // Đã cập nhật lại tên trường chuẩn xác có dấu
    school: { type: String, default: 'Trường Đại học Giao thông Vận tải TP.HCM (UTH)' },
    bio: { type: String, default: '' },
    
    // ==========================================
    // [CẬP NHẬT TRỌNG TÂM CHO BẢNG XẾP HẠNG]
    // ==========================================
    
    // Tổng số tài liệu đã upload thành công
    // Đánh index: true để API lấy bảng xếp hạng chạy nhanh hơn
    totalUploads: { type: Number, default: 0, index: true },
    
    // Điểm uy tín/tích lũy (Có thể dùng để tải tài liệu VIP, đổi thưởng sau này)
    reputationScore: { type: Number, default: 0, index: true },
    
    // [Tuỳ chọn] Danh hiệu của người dùng dựa trên số đóng góp
    level: { type: String, default: 'Tân binh' }
  },
  {
    // Tự động quản lý createdAt và updatedAt, không cần khai báo tay nữa
    timestamps: true 
  }
);

module.exports = mongoose.model('User', userSchema);