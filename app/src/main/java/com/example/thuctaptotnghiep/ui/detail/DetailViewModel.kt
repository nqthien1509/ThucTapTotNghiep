package com.example.thuctaptotnghiep.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.network.RetrofitClient // Lưu ý đường dẫn import này cho khớp với project của bạn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val _document = MutableStateFlow<Document?>(null)
    val document: StateFlow<Document?> = _document.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // =======================================================
    // THÊM MỚI: QUẢN LÝ TRẠNG THÁI NÚT BẤM
    // =======================================================
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isWatchLater = MutableStateFlow(false)
    val isWatchLater: StateFlow<Boolean> = _isWatchLater.asStateFlow()

    // Cập nhật hàm fetch để nhận thêm userId
    fun fetchDocumentDetail(id: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Truyền userId lên BE để biết user này đã lưu tài liệu chưa
                val result = RetrofitClient.apiService.getDocumentById(id, userId)
                _document.value = result

                // Khởi tạo trạng thái ban đầu cho 2 nút bấm dựa trên data BE trả về
                _isFavorite.value = result.isFavorite
                _isWatchLater.value = result.isWatchLater
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =======================================================
    // HÀM XỬ LÝ TƯƠNG TÁC (OPTIMISTIC UI)
    // =======================================================

    fun toggleFavorite(documentId: String, userId: String) {
        viewModelScope.launch {
            // Lưu lại trạng thái cũ
            val currentState = _isFavorite.value

            // Cập nhật UI ngay lập tức để tạo cảm giác mượt mà
            _isFavorite.value = !currentState

            try {
                // Gọi API ngầm dưới background
                val body = mapOf("userId" to userId)
                RetrofitClient.apiService.toggleFavorite(documentId, body)
            } catch (e: Exception) {
                // Nếu gọi API thất bại (mất mạng, lỗi server), tự động đảo ngược UI về như cũ
                _isFavorite.value = currentState
                _errorMessage.value = "Lỗi khi cập nhật yêu thích: ${e.message}"
            }
        }
    }

    fun toggleWatchLater(documentId: String, userId: String) {
        viewModelScope.launch {
            val currentState = _isWatchLater.value
            _isWatchLater.value = !currentState

            try {
                val body = mapOf("userId" to userId)
                RetrofitClient.apiService.toggleWatchLater(documentId, body)
            } catch (e: Exception) {
                _isWatchLater.value = currentState
                _errorMessage.value = "Lỗi khi cập nhật xem sau: ${e.message}"
            }
        }
    }
}