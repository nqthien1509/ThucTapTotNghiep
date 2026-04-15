package com.example.thuctaptotnghiep.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.repository.DocumentRepository // <-- Bắt buộc import
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel // <-- Bắt buộc import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Đánh dấu ViewModel được Hilt quản lý
class HomeViewModel @Inject constructor(
    private val repository: DocumentRepository // TIÊM REPOSITORY VÀO ĐÂY
) : ViewModel() {

    // LẤY TÊN THẬT TỪ FIREBASE ĐỂ TRUYỀN SANG GIAO DIỆN
    private val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName ?: "Người dùng"

    // 1. Khai báo các "Kho chứa" dữ liệu (StateFlow) để View quan sát
    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 2. Ngay khi ViewModel được tạo ra, tự động gọi API lấy dữ liệu
    init {
        fetchDocuments()
    }

    // 3. Hàm gọi API chạy dưới nền
    fun fetchDocuments() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // SỬA TẠI ĐÂY: Gọi Repository thay vì RetrofitClient
                val result = repository.getAllDocuments()
                _documents.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Hàm làm mới danh sách khi vuốt (Pull to refresh)
    fun refreshDocuments() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            try {
                val result = repository.getAllDocuments()
                _documents.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}