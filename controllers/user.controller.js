// controllers/user.controller.js
const userService = require('../services/user.service');

class UserController {
    // Lấy thông tin cá nhân
    async getProfile(req, res, next) {
        try {
            const uid = req.params.uid;
            const user = await userService.getUserProfile(uid);
            
            res.status(200).json(user);
        } catch (error) {
            if (error.message === 'USER_NOT_FOUND') {
                // Nếu là tài khoản mới tinh (có trên Firebase nhưng chưa có trong MongoDB)
                // Trả về một object mặc định để App Android không bị crash
                return res.status(200).json({ 
                    _id: req.params.uid, 
                    email: "", 
                    displayName: "", 
                    school: "Trường Đại học Giao thông vận tải TP.HCM (UTH)", 
                    bio: "", 
                    avatarUrl: "" 
                });
            }
            next(error); // Đẩy lỗi hệ thống ra ngoài cho Global Error Handler
        }
    }

    // Cập nhật thông tin chữ (Tên, Trường, Bio)
    async updateProfile(req, res, next) {
        try {
            const uid = req.params.uid;
            
            // Bảo mật (Authorization): Chặn không cho user này sửa profile của user khác
            if (uid !== req.user.uid) {
                req.log.warn({ uidParam: uid, tokenUid: req.user.uid }, 'Thử nghiệm sửa chéo tài khoản bị chặn');
                return res.status(403).json({ message: "Forbidden: Bạn không có quyền thao tác trên tài khoản người khác!" });
            }

            const updateData = req.body;
            
            // Xử lý bảo mật: Chặn không cho user tự sửa các trường nhạy cảm (VD: điểm số, role)
            delete updateData.points; 
            delete updateData.role;

            const user = await userService.updateUserProfile(uid, updateData);
            
            req.log.info({ userId: uid }, 'Cập nhật profile thành công');
            res.status(200).json(user);
        } catch (error) {
            next(error);
        }
    }

    // Cập nhật Ảnh đại diện
    async updateAvatar(req, res, next) {
        try {
            const uid = req.params.uid;
            
            // Bảo mật (Authorization): Chặn thao tác chéo
            if (uid !== req.user.uid) {
                return res.status(403).json({ message: "Forbidden: Bạn không có quyền thao tác trên tài khoản người khác!" });
            }

            // Kiểm tra xem middleware multer đã bắt được file chưa
            if (!req.file) {
                return res.status(400).json({ message: 'Vui lòng chọn một file ảnh hợp lệ!' });
            }

            const newAvatarUrl = '/uploads/' + req.file.filename;
            
            // Gọi service để lưu đường dẫn ảnh mới vào MongoDB
            const updatedUser = await userService.updateUserProfile(uid, { avatarUrl: newAvatarUrl });
            
            req.log.info({ userId: uid }, 'Cập nhật ảnh đại diện thành công');
            res.status(200).json(updatedUser);
        } catch (error) {
            next(error);
        }
    }
}

module.exports = new UserController();