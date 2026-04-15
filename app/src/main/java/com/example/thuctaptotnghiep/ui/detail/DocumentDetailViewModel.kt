package com.example.thuctaptotnghiep.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _document = MutableStateFlow<Document?>(null)
    val document: StateFlow<Document?> = _document.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // =======================================================
    // QUẢN LÝ TRẠNG THÁI NÚT BẤM
    // =======================================================
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isWatchLater = MutableStateFlow(false)
    val isWatchLater: StateFlow<Boolean> = _isWatchLater.asStateFlow()

    // CẬP NHẬT: Đã xóa tham số userId vì Backend tự định danh qua Token
    fun fetchDocumentDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Chỉ cần truyền id
                val result = repository.getDocumentById(id)
                _document.value = result

                // Khởi tạo trạng thái ban đầu cho 2 nút bấm dựa trên data BE trả về
                _isFavorite.value = result.isFavorite ?: false
                _isWatchLater.value = result.isWatchLater ?: false
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

    // CẬP NHẬT: Đã xóa tham số userId
    fun toggleFavorite(documentId: String) {
        viewModelScope.launch {
            val currentState = _isFavorite.value
            _isFavorite.value = !currentState // Cập nhật UI ngay lập tức

            try {
                // Không cần tạo Map body nữa, gọi thẳng repository
                repository.toggleFavorite(documentId)
            } catch (e: Exception) {
                // Nếu gọi API thất bại, đảo ngược UI về như cũ
                _isFavorite.value = currentState
                _errorMessage.value = "Lỗi khi cập nhật yêu thích: ${e.message}"
            }
        }
    }

    // CẬP NHẬT: Đã xóa tham số userId
    fun toggleWatchLater(documentId: String) {
        viewModelScope.launch {
            val currentState = _isWatchLater.value
            _isWatchLater.value = !currentState // Cập nhật UI ngay lập tức

            try {
                // Không cần tạo Map body nữa
                repository.toggleWatchLater(documentId)
            } catch (e: Exception) {
                _isWatchLater.value = currentState
                _errorMessage.value = "Lỗi khi cập nhật xem sau: ${e.message}"
            }
        }
    }
}