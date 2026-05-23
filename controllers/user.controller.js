// controllers/user.controller.js
const userService = require('../services/user.service');
const User = require('../models/User'); // [CẬP NHẬT]: Import thêm User model để truy vấn Bảng xếp hạng

class UserController {
    // 1. Lấy thông tin cá nhân
    async getProfile(req, res, next) {
        try {
            const uid = req.params.uid;

            // [CẬP NHẬT BẢO MẬT]: Ngăn chặn IDOR, chỉ cho phép user xem profile của chính mình
            if (uid !== req.user.uid) {
                req.log.warn({ uidParam: uid, tokenUid: req.user.uid }, 'Cảnh báo: Thử nghiệm xem chéo profile bị chặn');
                return res.status(403).json({ 
                    message: 'Forbidden: Bạn không có quyền xem thông tin của người dùng khác!' 
                });
            }

            const user = await userService.getUserProfile(uid);
            
            res.status(200).json(user);
        } catch (error) {
            if (error.message === 'USER_NOT_FOUND') {
                // Nếu là tài khoản mới tinh (có trên Firebase nhưng chưa có trong MongoDB)
                // [CẬP NHẬT]: Bổ sung thêm các trường điểm số mặc định
                return res.status(200).json({ 
                    _id: req.params.uid, 
                    email: "", 
                    displayName: "", 
                    school: "Trường Đại học Giao thông Vận tải TP.HCM (UTH)", 
                    bio: "", 
                    avatarUrl: "",
                    totalUploads: 0,
                    reputationScore: 0,
                    level: "Tân binh"
                });
            }
            next(error); // Đẩy lỗi hệ thống ra ngoài cho Global Error Handler
        }
    }

    // 2. Cập nhật thông tin chữ (Tên, Trường, Bio)
    async updateProfile(req, res, next) {
        try {
            const uid = req.params.uid;
            
            // Bảo mật (Authorization): Chặn không cho user này sửa profile của user khác
            if (uid !== req.user.uid) {
                req.log.warn({ uidParam: uid, tokenUid: req.user.uid }, 'Thử nghiệm sửa chéo tài khoản bị chặn');
                return res.status(403).json({ message: "Forbidden: Bạn không có quyền thao tác trên tài khoản người khác!" });
            }

            const updateData = req.body;
            
            // Xử lý bảo mật: Chặn không cho user tự sửa các trường nhạy cảm 
            // Ngăn chặn người dùng dùng API tự hack điểm của mình lên top
            delete updateData.points; 
            delete updateData.role;
            delete updateData.totalUploads;
            delete updateData.reputationScore;
            delete updateData.level;

            const user = await userService.updateUserProfile(uid, updateData);
            
            req.log.info({ userId: uid }, 'Cập nhật profile thành công');
            res.status(200).json(user);
        } catch (error) {
            next(error);
        }
    }

    // 3. Cập nhật Ảnh đại diện
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

    // ============================================================
    // [THÊM MỚI]: API LẤY BẢNG XẾP HẠNG NGƯỜI DÙNG ĐÓNG GÓP
    // ============================================================
    async getTopContributors(req, res, next) {
        try {
            // Lấy Top 10 người dùng sắp xếp theo số lượng Upload (giảm dần) 
            // Nếu bằng số upload thì xếp theo điểm uy tín (giảm dần)
            const topUsers = await User.find({})
                .sort({ totalUploads: -1, reputationScore: -1 })
                .limit(10)
                .select('displayName avatarUrl school totalUploads reputationScore level'); // Chỉ lấy dữ liệu cần thiết để bảo mật email

            res.status(200).json({ success: true, data: topUsers });
        } catch (error) {
            req.log.error({ err: error }, 'Lỗi khi lấy bảng xếp hạng người dùng');
            next(error);
        }
    }
}

module.exports = new UserController();