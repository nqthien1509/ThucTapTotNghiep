package com.example.thuctaptotnghiep.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.repository.DocumentRepository // Gọi đến kho dữ liệu
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // BẮT BUỘC: Báo cho Hilt biết đây là ViewModel cần quản lý
class SearchViewModel @Inject constructor(
    private val repository: DocumentRepository // TIÊM REPOSITORY VÀO ĐÂY
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // State quản lý Filter Chip (Category)
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

    // CẢI TIẾN 1: StateFlow chứa danh sách Category động
    private val _categories = MutableStateFlow<List<String>>(listOf("Tất cả"))
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // CẢI TIẾN 2: StateFlow chứa Lịch sử tìm kiếm
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // Biến lưu trữ tiến trình tìm kiếm hiện tại
    private var searchJob: Job? = null

    init {
        fetchCategories()
        loadRecentSearches()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                // TODO: Tương lai thay bằng repository.getCategories() từ Backend
                // Tạm thời dùng mock data, nhưng đã chuyển sang cơ chế dữ liệu động
                val mockCategories = listOf("Tất cả", "Công nghệ thông tin", "Kinh tế", "Ngoại ngữ", "Luật", "An ninh mạng")
                _categories.value = mockCategories
            } catch (e: Exception) {
                // Nếu lỗi mạng, ít nhất vẫn giữ lại được filter "Tất cả"
                _categories.value = listOf("Tất cả")
            }
        }
    }

    private fun loadRecentSearches() {
        // TODO: Lấy từ SharedPreferences hoặc DataStore để lưu vĩnh viễn
        // Mock data khởi tạo để dễ test giao diện
        _recentSearches.value = listOf("Đồ án tốt nghiệp", "Kotlin Jetpack Compose", "Tài liệu TOEIC")
    }

    // CẢI TIẾN 3: Thuật toán xử lý và lưu lịch sử tìm kiếm
    fun saveRecentSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return

        val currentList = _recentSearches.value.toMutableList()
        // Xóa từ khóa nếu đã tồn tại để cập nhật nó lên vị trí đầu tiên
        currentList.remove(trimmedQuery)
        currentList.add(0, trimmedQuery)

        // Giới hạn lưu 5 lịch sử gần nhất để UI không bị rác
        if (currentList.size > 5) {
            currentList.removeLast()
        }

        _recentSearches.value = currentList
        // TODO: Ghi đè list này xuống SharedPreferences/DataStore tại đây
    }

    // CẢI TIẾN 4: Hàm gọi khi người dùng chủ động bấm nút Tìm Kiếm trên bàn phím
    fun onSearchAction(query: String) {
        saveRecentSearch(query) // Lưu vào lịch sử
        _searchQuery.value = query
        executeSearch(withDelay = false) // Tìm ngay lập tức, bỏ qua delay
    }

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
        // HỦY ngay tiến trình tìm kiếm cũ nếu người dùng đang thao tác liên tục (Debounce)
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

            // Nếu đang gõ chữ thì đợi 0.5s xem có gõ tiếp không để tránh spam API
            if (withDelay) {
                delay(500)
            }

            try {
                // Nếu chọn "Tất cả" thì gửi 'null' lên Backend để không lọc loại tài liệu
                val categoryParam = if (currentCategory == "Tất cả") null else currentCategory

                val results = repository.searchDocuments(
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