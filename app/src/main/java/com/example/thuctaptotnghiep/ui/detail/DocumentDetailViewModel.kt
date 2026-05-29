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
    // TĂNG LƯỢT XEM VÀ TẢI (OPTIMISTIC UI)
    // =======================================================
    fun incrementViewCount(documentId: String) {
        viewModelScope.launch {
            try {
                // 1. Cập nhật UI ngay lập tức: Cộng thêm 1 vào views hiện tại
                _document.value = _document.value?.let { currentDoc ->
                    currentDoc.copy(views = (currentDoc.views ?: 0) + 1)
                }

                // 2. Gọi API để Backend lưu vào Database
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

    // =======================================================
    // [THÊM MỚI]: BÁO CÁO TÀI LIỆU VI PHẠM (REPORT DOCUMENT)
    // =======================================================
    fun reportDocument(
        targetId: String,
        reason: String,
        evidenceLink: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Truyền type = "document" cho hệ thống xử lý phân loại của backend
                val response = repository.createReport("document", targetId, reason, evidenceLink)
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess(response.body()?.message ?: "Gửi báo cáo tài liệu vi phạm thành công!")
                } else {
                    onError(response.body()?.message ?: "Không thể gửi báo cáo tài liệu")
                }
            } catch (e: Exception) {
                onError("Lỗi kết nối mạng: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}