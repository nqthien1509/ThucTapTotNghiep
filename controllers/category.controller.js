const Category = require('../models/Category');

class CategoryController {
    // 1. [PUBLIC] Lấy danh sách danh mục (Cho cả Web Admin và App Mobile gọi về)
    async getAllCategories(req, res) {
        try {
            const categories = await Category.find({}).sort({ createdAt: -1 });
            res.status(200).json({ success: true, data: categories });
        } catch (error) {
            res.status(500).json({ success: false, message: 'Lỗi server khi lấy danh mục!' });
        }
    }

    // 2. [ADMIN] Thêm danh mục mới
    async createCategory(req, res) {
        try {
            const { name, description } = req.body;
            if (!name) {
                return res.status(400).json({ success: false, message: 'Tên danh mục là bắt buộc!' });
            }

            const newCategory = new Category({ name, description });
            await newCategory.save();
            
            res.status(201).json({ success: true, data: newCategory, message: 'Đã thêm danh mục thành công!' });
        } catch (error) {
            if (error.code === 11000) {
                return res.status(400).json({ success: false, message: 'Tên danh mục này đã tồn tại!' });
            }
            res.status(500).json({ success: false, message: 'Lỗi server khi thêm danh mục!' });
        }
    }

    // 3. [ADMIN] Xóa danh mục
    async deleteCategory(req, res) {
        try {
            const { id } = req.params;
            await Category.findByIdAndDelete(id);
            res.status(200).json({ success: true, message: 'Đã xóa danh mục thành công!' });
        } catch (error) {
            res.status(500).json({ success: false, message: 'Lỗi server khi xóa danh mục!' });
        }
    }
}

module.exports = new CategoryController();