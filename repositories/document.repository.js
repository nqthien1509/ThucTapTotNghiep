const Document = require('../models/Document');

class DocumentRepository {
    async create(data) { return await new Document(data).save(); }
    async findById(id) { return await Document.findById(id); }
    async findAll() { return await Document.find().sort({ uploadDate: -1 }); }
    async findByQuery(query) { return await Document.find(query).sort({ uploadDate: -1 }); }
    async updateOne(filter, updateData) { return await Document.updateOne(filter, updateData); }
    async deleteById(id) { return await Document.findByIdAndDelete(id); }
}

module.exports = new DocumentRepository();