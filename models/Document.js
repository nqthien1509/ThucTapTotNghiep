const mongoose = require('mongoose');

const documentSchema = new mongoose.Schema({
    title: { type: String, required: true, trim: true },
    authorName: { type: String, required: true, trim: true },
    
    // --- 4 TRƯỜNG MỚI ---
    subject: { type: String, required: true }, 
    category: { 
        type: String, 
        enum: ['Slide', 'Đề thi', 'Giáo trình'], 
        required: true 
    }, 
    description: { type: String, trim: true }, 
    tags: { type: [String], default: [] }, 
    // -------------------

    fileUrl: { type: String, required: true },
    size: { type: String },
    uploadDate: { type: Date, default: Date.now },
    status: { type: String, default: 'pending', enum: ['pending', 'verified', 'failed'] },
    favoritedBy: { type: [String], default: [], index: true },
    watchLaterBy: { type: [String], default: [], index: true }
});

module.exports = mongoose.model('Document', documentSchema);