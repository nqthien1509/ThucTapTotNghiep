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

    // Trạng thái khóa nút (Chống click liên tục / Spam click)
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

    // Hàm làm sạch tên file để tải xuống (Sanitize Filename)
    fun getSafeFileName(originalTitle: String, extension: String = ".pdf"): String {
        val invalidChars = "[\\\\/:*?\"<>|]".toRegex()
        val safeName = originalTitle.replace(invalidChars, "_").trim()

        return if (safeName.isEmpty()) {
            "document_${System.currentTimeMillis()}$extension"
        } else {
            "$safeName$extension"
        }
    }

    // =======================================================
    // [CẬP NHẬT MỚI]: TĂNG LƯỢT XEM VÀ TẢI (OPTIMISTIC UI)
    // =======================================================
    fun incrementViewCount(documentId: String) {
        viewModelScope.launch {
            try {
                // 1. Cập nhật UI ngay lập tức: Cộng thêm 1 vào views hiện tại
                _document.value = _document.value?.let { currentDoc ->
                    // Giả sử views là Int. Nếu null thì mặc định 0 rồi cộng 1
                    currentDoc.copy(views = (currentDoc.views ?: 0) + 1)
                }

                // 2. Gọi API để Backend lưu vào Database
                // LƯU Ý: Bạn cần đảm bảo đã tạo hàm incrementView trong DocumentRepository nhé!
                repository.incrementView(documentId)
            } catch (e: Exception) {
                e.printStackTrace() // Lỗi đếm view thì chỉ in log, không cần gián đoạn người dùng
            }
        }
    }

    fun incrementDownloadCount(documentId: String) {
        viewModelScope.launch {
            try {
                // 1. Cập nhật UI ngay lập tức: Cộng thêm 1 vào downloads hiện tại
                _document.value = _document.value?.let { currentDoc ->
                    currentDoc.copy(downloads = (currentDoc.downloads ?: 0) + 1)
                }

                // 2. Gọi API để Backend lưu vào Database
                // LƯU Ý: Bạn cần đảm bảo đã tạo hàm incrementDownload trong DocumentRepository!
                repository.incrementDownload(documentId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // =======================================================
    // HÀM XỬ LÝ TƯƠNG TÁC (OPTIMISTIC UI + DEBOUNCE)
    // =======================================================

    fun toggleFavorite(documentId: String) {
        if (_isTogglingAction.value) return

        viewModelScope.launch {
            _isTogglingAction.value = true
            val currentState = _isFavorite.value
            _isFavorite.value = !currentState

            try {
                repository.toggleFavorite(documentId)
            } catch (e: Exception) {
                _isFavorite.value = currentState
                _errorMessage.value = "Lỗi khi cập nhật yêu thích: ${e.message}"
            } finally {
                _isTogglingAction.value = false
            }
        }
    }

    fun toggleWatchLater(documentId: String) {
        if (_isTogglingAction.value) return

        viewModelScope.launch {
            _isTogglingAction.value = true
            val currentState = _isWatchLater.value
            _isWatchLater.value = !currentState

            try {
                repository.toggleWatchLater(documentId)
            } catch (e: Exception) {
                _isWatchLater.value = currentState
                _errorMessage.value = "Lỗi khi cập nhật xem sau: ${e.message}"
            } finally {
                _isTogglingAction.value = false
            }
        }
    }
}