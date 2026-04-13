const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    _id: { type: String, required: true }, // Dùng chính UID của Firebase làm ID
    email: { type: String, required: true },
    displayName: { type: String, default: 'Người dùng ẩn danh' },
    avatarUrl: { type: String, default: '' },
    school: { type: String, default: 'Trường Đại học Giao thông vận tải TP.HCM (UTH)' },
    bio: { type: String, default: '' },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);