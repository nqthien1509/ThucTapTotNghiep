package com.example.thuctaptotnghiep.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth // ĐÃ THÊM IMPORT FIREBASE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    // LẤY TÊN THẬT TỪ FIREBASE ĐỂ TRUYỀN SANG GIAO DIỆN
    private val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName ?: "Người dùng"

    // 1. Khai báo các "Kho chứa" dữ liệu (StateFlow) để View quan sát
    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 2. Ngay khi ViewModel được tạo ra, tự động gọi API lấy dữ liệu
    init {
        fetchDocuments()
    }

    // 3. Hàm gọi API chạy dưới nền (viewModelScope)
    fun fetchDocuments() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Gọi API thông qua Retrofit
                val result = RetrofitClient.apiService.getDocuments()
                _documents.value = result // Đổ dữ liệu vào kho
            } catch (e: Exception) {
                _errorMessage.value = e.message // Nếu lỗi, báo cho View biết
            } finally {
                _isLoading.value = false
            }
        }
    }
}