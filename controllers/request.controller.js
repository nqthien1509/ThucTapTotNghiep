const Request = require('../models/Request');
const User = require('../models/User'); // Import model User để truy vấn

// 1. Lấy danh sách tất cả các yêu cầu đang mở (HÀM NÀY BỊ THIẾU Ở FILE CỦA BẠN)
exports.getRequests = async (req, res) => {
  try {
    const requests = await Request.find({ status: 'open' })
      .populate('author', 'displayName email avatarUrl') 
      .sort({ createdAt: -1 }); // Mới nhất lên đầu

    res.status(200).json({ success: true, data: requests });
  } catch (error) {
    console.error("LỖI LẤY YÊU CẦU:", error);
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};

// Đăng một yêu cầu tài liệu mới
exports.createRequest = async (req, res) => {
  try {
    const { title, description } = req.body;
    
    // Lấy Firebase UID từ token
    const firebaseUid = req.user.uid; 

    // Tìm User bằng _id 
    const currentUser = await User.findById(firebaseUid);
    
    if (!currentUser) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy thông tin người dùng trong CSDL' });
    }

    // Tạo bài đăng
    const newRequest = await Request.create({
      author: currentUser._id,
      title,
      description
    });

    // =========================================================
    // [THÊM DÒNG NÀY ĐỂ FIX LỖI ANDROID]: 
    // Nhồi thông tin User vào bài đăng vừa tạo trước khi trả về
    await newRequest.populate('author', 'displayName email avatarUrl');
    // =========================================================

    res.status(201).json({ success: true, data: newRequest });
  } catch (error) {
    console.error("LỖI TẠO YÊU CẦU:", error);
    res.status(500).json({ success: false, message: 'Lỗi khi tạo yêu cầu', error: error.message });
  }
};