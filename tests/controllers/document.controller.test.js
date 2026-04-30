const documentController = require('../../controllers/document.controller');
const docRepo = require('../../repositories/document.repository');

// Mock tầng kết nối Database
jest.mock('../../repositories/document.repository');

describe('Document Controller - Bảo mật lộ tài liệu', () => {
    let mockReq, mockRes, nextFunction;

    beforeEach(() => {
        mockReq = { params: { id: 'doc_123' }, user: { uid: 'user_A' } };
        mockRes = { status: jest.fn().mockReturnThis(), json: jest.fn() };
        nextFunction = jest.fn();
    });

    it('Nên chặn HTTP 403 nếu tài liệu chưa duyệt (pending) và người xem KHÔNG phải chủ sở hữu', async () => {
        // Giả lập DB trả về tài liệu của user_B, trạng thái pending
        docRepo.findById.mockResolvedValue({
            _id: 'doc_123',
            userId: 'user_B',
            status: 'pending'
        });

        await documentController.getDetail(mockReq, mockRes, nextFunction);

        expect(mockRes.status).toHaveBeenCalledWith(403);
        expect(mockRes.json).toHaveBeenCalledWith(expect.objectContaining({
            message: expect.stringContaining('bạn không có quyền xem')
        }));
    });
});