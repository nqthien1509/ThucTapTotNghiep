const docRepo = require('../repositories/document.repository');
const fs = require('fs');
const path = require('path');

class DocumentService {
    async toggleFavorite(docId, userId) {
        const doc = await docRepo.findById(docId);
        if (!doc) throw new Error('NOT_FOUND');
        const isFavorited = doc.favoritedBy.includes(userId);
        await docRepo.updateOne({ _id: docId }, isFavorited ? { $pull: { favoritedBy: userId } } : { $push: { favoritedBy: userId } });
        return !isFavorited;
    }

    async toggleWatchLater(docId, userId) {
        const doc = await docRepo.findById(docId);
        if (!doc) throw new Error('NOT_FOUND');
        const isWatchLater = doc.watchLaterBy.includes(userId);
        await docRepo.updateOne({ _id: docId }, isWatchLater ? { $pull: { watchLaterBy: userId } } : { $push: { watchLaterBy: userId } });
        return !isWatchLater;
    }

    async deleteDocument(docId, userId) {
        const doc = await docRepo.findById(docId);
        if (!doc) throw new Error('NOT_FOUND');
        if (doc.userId !== userId) throw new Error('FORBIDDEN');

        const filePath = path.join(__dirname, '../uploads', path.basename(doc.fileUrl));
        if (fs.existsSync(filePath)) fs.unlinkSync(filePath);
        await docRepo.deleteById(docId);
    }
}

module.exports = new DocumentService();