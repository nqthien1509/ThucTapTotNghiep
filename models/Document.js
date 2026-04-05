const mongoose = require('mongoose');

// Định nghĩa cấu trúc của một Tài liệu (Document)
const documentSchema = new mongoose.Schema({
    title: { type: String, required: true },        // Tên tài liệu
    authorName: { type: String, required: true },   // Tên người đăng
    fileUrl: { type: String, required: true },      // Đường dẫn để tải file PDF
    size: { type: String },                         // Dung lượng (VD: "2.5 MB")
    uploadDate: { type: Date, default: Date.now },  // Ngày đăng
    downloads: { type: Number, default: 0 },        // Số lượt tải
    views: { type: Number, default: 0 }             // Số lượt xem
});

// BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ SERVER NHẬN DIỆN ĐƯỢC
module.exports = mongoose.model('Document', documentSchema);