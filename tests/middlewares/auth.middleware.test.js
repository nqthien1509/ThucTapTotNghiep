const { verifyToken } = require('../../middlewares/auth.middleware');
const admin = require('firebase-admin');

// Mock Firebase Admin
jest.mock('firebase-admin', () => ({
    auth: jest.fn().mockReturnValue({
        verifyIdToken: jest.fn()
    })
}));

describe('Auth Middleware', () => {
    let mockReq, mockRes, nextFunction;

    beforeEach(() => {
        // Đã thêm mock cho logger để không bị văng lỗi khi middleware gọi req.log.warn
        mockReq = { headers: {}, log: { warn: jest.fn(), debug: jest.fn() } };
        mockRes = { status: jest.fn().mockReturnThis(), json: jest.fn() };
        nextFunction = jest.fn();
    });

    it('Nên trả về 401 nếu thiếu header Authorization', async () => {
        await verifyToken(mockReq, mockRes, nextFunction);
        
        expect(mockRes.status).toHaveBeenCalledWith(401);
        expect(mockRes.json).toHaveBeenCalledWith({ message: 'Unauthorized: Thiếu hoặc sai định dạng token!' });
        expect(nextFunction).not.toHaveBeenCalled();
    });

    it('Nên gọi next() và gán req.user nếu token hợp lệ', async () => {
        mockReq.headers.authorization = 'Bearer valid_token_123';
        const decodedToken = { uid: 'user_123', email: 'test@example.com' };
        
        // Giả lập Firebase xác thực thành công
        admin.auth().verifyIdToken.mockResolvedValueOnce(decodedToken);

        await verifyToken(mockReq, mockRes, nextFunction);

        // CẬP NHẬT: Đã sửa req.user thành mockReq.user
        expect(mockReq.user).toEqual(decodedToken);
        expect(nextFunction).toHaveBeenCalled();
    });
});