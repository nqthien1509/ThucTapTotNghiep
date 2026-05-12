const Conversation = require('../models/Conversation');
const Message = require('../models/Message');
const User = require('../models/User');

// 1. Hàm tạo hoặc lấy phòng chat đã có
exports.getOrCreateConversation = async (req, res) => {
  try {
    const { requestId, receiverId } = req.body;
    const firebaseUid = req.user.uid;

    // ===============================================================
    // [CƠ CHẾ TỰ ĐỘNG ĐỒNG BỘ]: Tìm User, nếu không có thì tự tạo mới
    // ===============================================================
    let currentUser = await User.findById(firebaseUid);
    
    if (!currentUser) {
        console.log(`[TỰ ĐỘNG ĐỒNG BỘ] Đang tạo User mới vào CSDL: ${firebaseUid}`);
        currentUser = await User.create({
            _id: firebaseUid,
            email: req.user.email || 'email_an_danh@gmail.com',
            displayName: req.user.name || 'Học viên ẩn danh',
            avatarUrl: req.user.picture || ''
        });
    }

    const senderId = currentUser._id;

    // Tìm xem đã có phòng chat giữa 2 người này cho yêu cầu này chưa
    let conversation = await Conversation.findOne({
        requestId: requestId,
        participants: { $all: [senderId, receiverId] }
    });

    // Nếu chưa có thì tạo phòng mới
    if (!conversation) {
        conversation = await Conversation.create({
            requestId: requestId,
            participants: [senderId, receiverId]
        });
    }

    // =========================================================
    // [QUAN TRỌNG NHẤT ĐỂ FIX LỖI ANDROID]: 
    // Biến participants từ String ID thành Object User
    // =========================================================
    await conversation.populate('participants', 'displayName avatarUrl email');

    res.status(200).json({ success: true, data: conversation });
  } catch (error) {
    console.error("LỖI TẠO PHÒNG CHAT:", error);
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};

// 2. Hàm lấy lịch sử tin nhắn
exports.getMessages = async (req, res) => {
  try {
    const { conversationId } = req.params;
    const messages = await Message.find({ conversationId }).sort({ createdAt: 1 });
    
    res.status(200).json({ success: true, data: messages });
  } catch (error) {
    console.error("LỖI LẤY TIN NHẮN:", error);
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};

// 3. Hàm lấy danh sách phòng chat của 1 User (Hộp thư Inbox)
exports.getConversations = async (req, res) => {
  try {
    const firebaseUid = req.user.uid;
    const currentUser = await User.findById(firebaseUid);
    
    if (!currentUser) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }

    // Tìm tất cả phòng chat có sự tham gia của User này
    // populate('participants') để lấy luôn tên và ảnh đại diện của người kia hiển thị lên danh sách
    const conversations = await Conversation.find({
        participants: currentUser._id
    })
    .populate('participants', 'displayName avatarUrl email')
    .sort({ updatedAt: -1 }); // Xếp phòng chat mới nhắn lên đầu

    res.status(200).json({ success: true, data: conversations });
  } catch (error) {
    console.error("LỖI LẤY DANH SÁCH INBOX:", error);
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};