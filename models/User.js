﻿const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    _id: { type: String, required: true },
    email: { type: String, default: '' },
    displayName: { type: String, default: 'Nguoi dung an danh' },
    avatarUrl: { type: String, default: '' },
    school: { type: String, default: 'Truong Dai hoc Giao thong van tai TP.HCM (UTH)' },
    bio: { type: String, default: '' },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);