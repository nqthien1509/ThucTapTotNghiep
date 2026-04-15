// repositories/user.repository.js
const User = require('../models/User'); 

class UserRepository {
    // Tìm user theo Firebase UID (Lưu ở trường _id)
    async findByUid(uid) {
        return await User.findById(uid);
    }

    // Cập nhật thông tin user (Tạo mới nếu chưa tồn tại nhờ upsert: true)
    async updateByUid(uid, updateData) {
        return await User.findByIdAndUpdate(
            uid, 
            updateData, 
            { new: true, upsert: true } // new: trả về data sau khi update, upsert: tạo mới nếu không tìm thấy
        );
    }
}

module.exports = new UserRepository(); // Xuất ra một instance duy nhất (Singleton)