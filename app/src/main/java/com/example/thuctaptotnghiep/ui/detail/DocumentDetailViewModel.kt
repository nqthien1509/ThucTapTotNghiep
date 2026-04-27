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

    // CẢI TIẾN 1: Trạng thái khóa nút (Chống click liên tục / Spam click)
    private val _isTogglingAction = MutableStateFlow(false)
    val isTogglingAction: StateFlow<Boolean> = _isTogglingAction.asStateFlow()

    fun fetchDocumentDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
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

    // CẢI TIẾN 2: Hàm làm sạch tên file để tải xuống (Sanitize Filename)
    fun getSafeFileName(originalTitle: String, extension: String = ".pdf"): String {
        // Regex tìm các ký tự không được phép đặt tên file trong hệ thống
        val invalidChars = "[\\\\/:*?\"<>|]".toRegex()
        val safeName = originalTitle.replace(invalidChars, "_").trim()

        return if (safeName.isEmpty()) {
            "document_${System.currentTimeMillis()}$extension"
        } else {
            "$safeName$extension"
        }
    }

    // =======================================================
    // HÀM XỬ LÝ TƯƠNG TÁC (OPTIMISTIC UI + DEBOUNCE)
    // =======================================================

    fun toggleFavorite(documentId: String) {
        // Ngăn chặn gọi API liên tục nếu đang xử lý request trước đó
        if (_isTogglingAction.value) return

        viewModelScope.launch {
            _isTogglingAction.value = true
            val currentState = _isFavorite.value
            _isFavorite.value = !currentState // Cập nhật UI ngay lập tức

            try {
                repository.toggleFavorite(documentId)
            } catch (e: Exception) {
                // Nếu gọi API thất bại, đảo ngược UI về như cũ
                _isFavorite.value = currentState
                _errorMessage.value = "Lỗi khi cập nhật yêu thích: ${e.message}"
            } finally {
                // Mở khóa nút khi hoàn tất luồng
                _isTogglingAction.value = false
            }
        }
    }

    fun toggleWatchLater(documentId: String) {
        // Ngăn chặn gọi API liên tục nếu đang xử lý request trước đó
        if (_isTogglingAction.value) return

        viewModelScope.launch {
            _isTogglingAction.value = true
            val currentState = _isWatchLater.value
            _isWatchLater.value = !currentState // Cập nhật UI ngay lập tức

            try {
                repository.toggleWatchLater(documentId)
            } catch (e: Exception) {
                // Nếu gọi API thất bại, đảo ngược UI về như cũ
                _isWatchLater.value = currentState
                _errorMessage.value = "Lỗi khi cập nhật xem sau: ${e.message}"
            } finally {
                // Mở khóa nút khi hoàn tất luồng
                _isTogglingAction.value = false
            }
        }
    }
}