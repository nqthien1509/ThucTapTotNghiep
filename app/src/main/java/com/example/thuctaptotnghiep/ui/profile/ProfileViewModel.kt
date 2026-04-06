package com.example.thuctaptotnghiep.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _myDocuments = MutableStateFlow<List<Document>>(emptyList())
    val myDocuments: StateFlow<List<Document>> = _myDocuments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // =======================================================
    // THÊM STATE QUẢN LÝ VUỐT LÀM MỚI (PULL-TO-REFRESH)
    // =======================================================
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _deleteStatus = MutableStateFlow<String?>(null)
    val deleteStatus: StateFlow<String?> = _deleteStatus.asStateFlow()

    // LẤY TÊN THẬT TỪ FIREBASE VÀ KIỂM TRA CHẶT CHẼ
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val rawName = currentUser?.displayName
    // Nếu rawName bị null hoặc rỗng (""), lập tức dùng tên mặc định để tránh lỗi 404
    val userName = if (rawName.isNullOrBlank()) "Người dùng ẩn danh" else rawName

    init {
        loadMyDocuments()
    }

    // Hàm load dữ liệu lần đầu (Sử dụng vòng xoay lớn giữa màn hình)
    fun loadMyDocuments() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Truyền tên đã được kiểm tra an toàn vào API
                val result = RetrofitClient.apiService.getMyDocuments(userName)
                _myDocuments.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =======================================================
    // HÀM MỚI: Tải lại dữ liệu khi vuốt (Sử dụng vòng xoay nhỏ)
    // =======================================================
    fun refreshDocuments() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            try {
                // Gọi lại API để cập nhật trạng thái mới nhất (từ RabbitMQ)
                val result = RetrofitClient.apiService.getMyDocuments(userName)
                _myDocuments.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deleteDocument(id)
                _deleteStatus.value = "Đã xóa thành công!"
                loadMyDocuments() // Tải lại danh sách sau khi xóa
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi xóa: ${e.message}"
            }
        }
    }

    fun resetDeleteStatus() {
        _deleteStatus.value = null
    }
}