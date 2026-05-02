jest.mock('../../services/document.service', () => ({
    toggleFavorite: jest.fn()
}));

jest.mock('../../services/notification.service', () => ({
    notifyDocumentFavorited: jest.fn()
}));

const documentController = require('../../controllers/document.controller');
const docService = require('../../services/document.service');
const { notifyDocumentFavorited } = require('../../services/notification.service');

describe('Document Controller - Favorite notification', () => {
    let mockReq;
    let mockRes;
    let nextFunction;

    beforeEach(() => {
        mockReq = {
            params: { id: 'doc_001' },
            user: { uid: 'liker_uid' },
            log: { error: jest.fn(), info: jest.fn() }
        };
        mockRes = { status: jest.fn().mockReturnThis(), json: jest.fn() };
        nextFunction = jest.fn();
        jest.clearAllMocks();
    });

    it('sends notification when favorite is added', async () => {
        docService.toggleFavorite.mockResolvedValue({
            isAdded: true,
            document: { _id: 'doc_001', userId: 'owner_uid', title: 'Tailieu A' }
        });

        await documentController.toggleFavorite(mockReq, mockRes, nextFunction);

        expect(notifyDocumentFavorited).toHaveBeenCalledTimes(1);
        expect(mockRes.status).toHaveBeenCalledWith(200);
    });

    it('does not send notification when favorite is removed', async () => {
        docService.toggleFavorite.mockResolvedValue({
            isAdded: false,
            document: { _id: 'doc_001', userId: 'owner_uid', title: 'Tailieu A' }
        });

        await documentController.toggleFavorite(mockReq, mockRes, nextFunction);

        expect(notifyDocumentFavorited).not.toHaveBeenCalled();
        expect(mockRes.status).toHaveBeenCalledWith(200);
    });
});
