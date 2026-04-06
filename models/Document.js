const mongoose = require('mongoose');

// Định nghĩa cấu trúc của một Tài liệu (Document)
const documentSchema = new mongoose.Schema({
    title: { 
        type: String, 
        required: true,
        trim: true // Tự động xóa khoảng trắng thừa ở đầu/cuối
    }, 
    authorName: { 
        type: String, 
        required: true,
        trim: true 
    }, 
    fileUrl: { 
        type: String, 
        required: true 
    }, 
    size: { 
        type: String 
    }, 
    uploadDate: { 
        type: Date, 
        default: Date.now 
    }, 
    downloads: { 
        type: Number, 
        default: 0 
    }, 
    views: { 
        type: Number, 
        default: 0 
    },
    status: { 
        type: String, 
        default: 'pending',
        enum: ['pending', 'verified', 'failed'] // Chỉ cho phép 1 trong 3 giá trị này
    }
});

// Xuất Model để sử dụng trong Server và Worker
module.exports = mongoose.model('Document', documentSchema);