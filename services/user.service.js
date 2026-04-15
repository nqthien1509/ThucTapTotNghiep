// services/user.service.js
const userRepository = require('../repositories/user.repository');

class UserService {
    async getUserProfile(uid) {
        const user = await userRepository.findByUid(uid);
        if (!user) {
            throw new Error('USER_NOT_FOUND'); // Ném lỗi văng ra cho Controller bắt
        }
        return user;
    }

    async updateUserProfile(uid, data) {
        const updatedUser = await userRepository.updateByUid(uid, data);
        if (!updatedUser) {
            throw new Error('USER_NOT_FOUND');
        }
        return updatedUser;
    }
}

module.exports = new UserService();