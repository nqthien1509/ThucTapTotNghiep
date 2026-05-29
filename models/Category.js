const mongoose = require('mongoose');

const categorySchema = new mongoose.Schema({
    name: { 
        type: String, 
        required: true, 
        unique: true, // Không cho phép trùng tên môn học
        trim: true 
    },
    description: { 
        type: String,
        default: ''
    },
    createdAt: { 
        type: Date, 
        default: Date.now 
    }
});

module.exports = mongoose.model('Category', categorySchema);