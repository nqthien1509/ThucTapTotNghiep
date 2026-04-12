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

    // Hàm được gọi mỗi khi người dùng gõ thêm 1 chữ
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery

        // HỦY ngay tiến trình tìm kiếm cũ nếu người dùng gõ liên tục chưa nghỉ
        searchJob?.cancel()

        if (newQuery.trim().isEmpty()) {
            _searchResults.value = emptyList()
            _hasSearched.value = false
            _isSearching.value = false
            return
        }

        // Tạo tiến trình tìm kiếm mới
        searchJob = viewModelScope.launch {
            _isSearching.value = true

            delay(500) // Đợi 0.5s xem người dùng có gõ tiếp không (Debounce)

            try {
                // Nếu sau 0.5s mà tiến trình không bị hủy (người dùng đã ngừng gõ), thì gọi API!
                val results = RetrofitClient.apiService.searchDocuments(newQuery.trim())
                _searchResults.value = results
                _hasSearched.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isSearching.value = false
            }
        }
    }
}