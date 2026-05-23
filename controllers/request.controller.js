const Request = require('../models/Request');
const User = require('../models/User'); 

// 1. Lấy danh sách tất cả các yêu cầu đang mở 
exports.getRequests = async (req, res) => {
  try {
    const requests = await Request.find({ status: 'open' })
      .populate('author', 'displayName email avatarUrl') 
      .populate('comments.user', 'displayName avatarUrl level') // [FIX]: Giải mã thông tin người bình luận
      .sort({ createdAt: -1 }); 

    requests.sort((a, b) => (b.upvotes?.length || 0) - (a.upvotes?.length || 0));

    res.status(200).json({ success: true, data: requests });
  } catch (error) {
    console.error("LỖI LẤY YÊU CẦU:", error);
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};

// ============================================================
// 2. Lấy chi tiết 1 bài viết kèm theo tất cả Bình luận
// ============================================================
exports.getRequestById = async (req, res) => {
  try {
    const request = await Request.findById(req.params.id)
      .populate('author', 'displayName email avatarUrl level')
      .populate('resolvedBy', 'displayName email avatarUrl')
      .populate('comments.user', 'displayName avatarUrl level'); 

    if (!request) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy yêu cầu' });
    }
    res.status(200).json({ success: true, data: request });
  } catch (error) {
    console.error("LỖI LẤY CHI TIẾT YÊU CẦU:", error);
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};

// 3. Đăng một yêu cầu tài liệu mới
exports.createRequest = async (req, res) => {
  try {
    const { title, description } = req.body;
    const firebaseUid = req.user.uid; 
    const currentUser = await User.findById(firebaseUid);
    
    if (!currentUser) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy thông tin người dùng' });
    }

    const newRequest = await Request.create({
      author: currentUser._id,
      title,
      description
    });

    // Nhồi thông tin User vào bài đăng vừa tạo 
    await newRequest.populate('author', 'displayName email avatarUrl');
    await newRequest.populate('comments.user', 'displayName avatarUrl level'); // Chống crash

    res.status(201).json({ success: true, data: newRequest });
  } catch (error) {
    console.error("LỖI TẠO YÊU CẦU:", error);
    res.status(500).json({ success: false, message: 'Lỗi khi tạo yêu cầu', error: error.message });
  }
};

// 4. Xử lý Upvote 
exports.upvoteRequest = async (req, res) => {
  try {
    const requestId = req.params.id;
    const userId = req.user.uid; 

    const request = await Request.findById(requestId);
    if (!request) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy yêu cầu' });
    }

    const hasUpvoted = request.upvotes.includes(userId);
    if (hasUpvoted) {
      request.upvotes = request.upvotes.filter(id => id !== userId);
    } else {
      request.upvotes.push(userId);
    }

    await request.save();
    
    // [FIX]: Populate đầy đủ thông tin trước khi trả về để Android GSON không bị crash
    await request.populate('author', 'displayName email avatarUrl');
    await request.populate('comments.user', 'displayName avatarUrl level');

    res.status(200).json({ 
        success: true, 
        message: hasUpvoted ? 'Đã bỏ upvote' : 'Đã upvote',
        data: request
    });
  } catch (error) {
    console.error("LỖI UPVOTE:", error);
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};

// 5. Đóng yêu cầu (Khi có người cung cấp link tài liệu)
exports.resolveRequest = async (req, res) => {
  try {
    const requestId = req.params.id;
    const userId = req.user.uid;
    const { resolvedLink } = req.body;

    if (!resolvedLink) {
      return res.status(400).json({ success: false, message: 'Vui lòng cung cấp link tài liệu' });
    }

    const request = await Request.findById(requestId);
    if (!request) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy yêu cầu' });
    }

    request.status = 'resolved';
    request.resolvedLink = resolvedLink;
    request.resolvedBy = userId;

    await request.save();
    
    // [FIX]: Lấy thêm thông tin đầy đủ để chống crash
    await request.populate('resolvedBy', 'displayName email avatarUrl');
    await request.populate('author', 'displayName email avatarUrl');
    await request.populate('comments.user', 'displayName avatarUrl level');

    res.status(200).json({ 
        success: true, 
        message: 'Đã giải quyết yêu cầu', 
        data: request 
    });
  } catch (error) {
    console.error("LỖI RESOLVE YÊU CẦU:", error);
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};

// ============================================================
// 6. Thêm bình luận vào diễn đàn bài viết
// ============================================================
exports.addComment = async (req, res) => {
  try {
    const { content } = req.body;
    if (!content || content.trim() === '') {
      return res.status(400).json({ success: false, message: 'Bình luận không được để trống' });
    }

    const request = await Request.findById(req.params.id);
    if (!request) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy bài viết' });
    }

    request.comments.push({
      user: req.user.uid,
      content: content
    });

    await request.save();

    // [FIX]: Populate đầy đủ thông tin author và user của bình luận
    await request.populate('author', 'displayName email avatarUrl');
    await request.populate('comments.user', 'displayName avatarUrl level');

    res.status(201).json({ success: true, message: 'Đã bình luận', data: request });
  } catch (error) {
    console.error("LỖI THÊM BÌNH LUẬN:", error);
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};