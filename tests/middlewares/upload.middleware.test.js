// CẬP NHẬT: Import trực tiếp pdfFilter thay vì uploadPdf
const { pdfFilter } = require('../../middlewares/upload.middleware');

describe('Upload Validation', () => {
    let mockReq, cb;

    beforeEach(() => {
        mockReq = {};
        cb = jest.fn();
    });

    it('Nên chấp nhận file có mimetype là application/pdf', () => {
        const mockFile = { mimetype: 'application/pdf' };
        
        // CẬP NHẬT: Gọi thẳng hàm pdfFilter
        pdfFilter(mockReq, mockFile, cb);

        expect(cb).toHaveBeenCalledWith(null, true); // (Lỗi null, Cho phép qua)
    });

    it('Nên từ chối file nếu không phải là PDF (VD: Hình ảnh)', () => {
        const mockFile = { mimetype: 'image/jpeg' };
        
        // CẬP NHẬT: Gọi thẳng hàm pdfFilter
        pdfFilter(mockReq, mockFile, cb);

        expect(cb).toHaveBeenCalledWith(expect.any(Error), false);
        expect(cb.mock.calls[0][0].message).toContain('INVALID_FILE_TYPE');
    });
});