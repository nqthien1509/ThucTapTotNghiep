package com.example.thuctaptotnghiep.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ==========================================
    // THÊM MỚI: State quản lý Filter Chip (Category)
    // ==========================================
    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Document>>(emptyList())
    val searchResults: StateFlow<List<Document>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Biến lưu trữ tiến trình tìm kiếm hiện tại
    private var searchJob: Job? = null

    // 1. Hàm được gọi mỗi khi người dùng gõ chữ (Bật tính năng đợi 0.5s)
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        executeSearch(withDelay = true)
    }

    // 2. Hàm được gọi khi bấm vào Filter Chip (Tắt đợi 0.5s -> Tìm ngay lập tức)
    fun onCategorySelected(newCategory: String) {
        _selectedCategory.value = newCategory
        executeSearch(withDelay = false)
    }

    // HÀM XỬ LÝ TÌM KIẾM CHUNG (GỘP CHUNG API)
    private fun executeSearch(withDelay: Boolean) {
        // HỦY ngay tiến trình tìm kiếm cũ nếu người dùng đang thao tác liên tục
        searchJob?.cancel()

        val currentQuery = _searchQuery.value.trim()
        val currentCategory = _selectedCategory.value

        // Nếu không gõ chữ VÀ đang chọn "Tất cả" -> Xóa trắng màn hình (không gọi API)
        if (currentQuery.isEmpty() && currentCategory == "Tất cả") {
            _searchResults.value = emptyList()
            _hasSearched.value = false
            _isSearching.value = false
            return
        }

        // Tạo tiến trình tìm kiếm mới
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            _errorMessage.value = null

            // Nếu đang gõ chữ thì đợi 0.5s xem có gõ tiếp không
            if (withDelay) {
                delay(500)
            }

            try {
                // Nếu chọn "Tất cả" thì gửi 'null' lên Backend để không lọc loại tài liệu
                val categoryParam = if (currentCategory == "Tất cả") null else currentCategory

                // Gọi API với cả 2 tham số: Chữ và Môn
                val results = RetrofitClient.apiService.searchDocuments(
                    keyword = currentQuery,
                    category = categoryParam
                )

                _searchResults.value = results
                _hasSearched.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _searchResults.value = emptyList() // Lỗi thì trả về rỗng
            } finally {
                _isSearching.value = false
            }
        }
    }
}