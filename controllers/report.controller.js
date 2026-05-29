const Report = require('../models/Report');
const Document = require('../models/Document');
const User = require('../models/User');

// ==========================================
// 1. CHO NGƯỜI DÙNG: Tạo một báo cáo vi phạm
// ==========================================
exports.createReport = async (req, res) => {
  try {
    const { type, targetId, reason, evidenceLink } = req.body;
    const reporterId = req.user.uid; // Lấy từ verifyToken middleware

    if (!type || !reason || !targetId) {
      return res.status(400).json({ success: false, message: 'Vui lòng điền đầy đủ thông tin báo cáo!' });
    }

    const reportData = {
      reporter: reporterId,
      type,
      reason,
      evidenceLink
    };

    if (type === 'document') {
      reportData.targetDocument = targetId;
    } else if (type === 'user') {
      reportData.targetUser = targetId;
    }

    const newReport = await Report.create(reportData);
    
    res.status(201).json({ 
      success: true, 
      message: 'Gửi báo cáo vi phạm thành công. Ban quản trị sẽ sớm xử lý!', 
      data: newReport 
    });
  } catch (error) {
    console.error("LỖI TẠO BÁO CÁO VI PHẠM:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi tạo báo cáo', error: error.message });
  }
};

// ==========================================
// 2. CHO ADMIN: Lấy danh sách toàn bộ báo cáo vi phạm
// ==========================================
exports.getAllReports = async (req, res) => {
  try {
    const reports = await Report.find()
      .populate('reporter', 'displayName email avatarUrl')
      .populate('targetDocument', 'title fileUrl status')
      .populate('targetUser', 'displayName email role isBlocked')
      .sort({ createdAt: -1 });

    res.status(200).json({ success: true, data: reports });
  } catch (error) {
    console.error("LỖI LẤY DANH SÁCH BÁO CÁO:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ không thể lấy danh sách báo cáo', error: error.message });
  }
};

// ==========================================
// 3. CHO ADMIN: Xử lý Đơn báo cáo (Phạt hoặc Bỏ qua)
// ==========================================
exports.actionReport = async (req, res) => {
  try {
    const { status, adminNotes, blockTarget } = req.body; 
    // status: 'resolved' (Đã xử lý) hoặc 'dismissed' (Bác đơn/Bỏ qua)
    // blockTarget: true nếu admin muốn khóa tài khoản / ẩn file ngay lập tức
    
    const report = await Report.findById(req.params.id);

    if (!report) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy đơn báo cáo này' });
    }

    report.status = status;
    report.adminNotes = adminNotes || '';
    await report.save();

    // Nếu admin áp dụng hình phạt thực tế:
    if (blockTarget) {
      if (report.type === 'document') {
        // Chuyển trạng thái tài liệu về 'failed' (không được kiểm duyệt/bị ẩn)
        await Document.findByIdAndUpdate(report.targetDocument, { status: 'failed' });
      } else if (report.type === 'user') {
        // Khóa tài khoản của người dùng có lời lẽ khiếm nhã
        await User.findByIdAndUpdate(report.targetUser, { isBlocked: true }); 
      }
    }

    res.status(200).json({ 
      success: true, 
      message: 'Đã cập nhật trạng thái xử lý báo cáo thành công!', 
      data: report 
    });
  } catch (error) {
    console.error("LỖI XỬ LÝ BÁO CÁO:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi xử lý báo cáo', error: error.message });
  }
};