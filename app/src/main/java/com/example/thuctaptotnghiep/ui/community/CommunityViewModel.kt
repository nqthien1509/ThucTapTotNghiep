package com.example.thuctaptotnghiep.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Request
import com.example.thuctaptotnghiep.data.repository.CommunityRepository
import com.example.thuctaptotnghiep.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    // Danh sách bài đăng (Dành cho màn hình chính của Cộng đồng)
    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests.asStateFlow()

    // =============================================================
    // [THÊM MỚI]: LƯU TRỮ TRẠNG THÁI CHI TIẾT CỦA BÀI VIẾT (DIỄN ĐÀN)
    // =============================================================
    private val _selectedRequest = MutableStateFlow<Request?>(null)
    val selectedRequest: StateFlow<Request?> = _selectedRequest.asStateFlow()

    // Trạng thái load dữ liệu
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Trạng thái lỗi (nếu có)
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Vừa vào màn hình là tự động gọi API lấy dữ liệu luôn
        fetchRequests()
    }

    // 1. Lấy danh sách toàn bộ các yêu cầu
    fun fetchRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.getRequests()
                if (response.isSuccessful && response.body()?.success == true) {
                    _requests.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = response.body()?.message ?: "Không thể tải danh sách cộng đồng"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Lỗi kết nối mạng"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. Tạo một yêu cầu mới
    fun createRequest(title: String, description: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.createRequest(title, description)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchRequests() // Tạo xong thì tải lại danh sách cho mới
                    onSuccess()     // Báo cho UI biết để đóng Dialog/Chuyển màn hình
                } else {
                    _error.value = "Lỗi khi đăng bài"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Lỗi kết nối mạng"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =============================================================
    // [THÊM MỚI]: CÁC HÀM XỬ LÝ DIỄN ĐÀN THẢO LUẬN
    // =============================================================

    // 3. Lấy chi tiết một bài viết (Bao gồm danh sách bình luận)
    fun fetchRequestDetail(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.getRequestById(requestId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _selectedRequest.value = response.body()?.data
                } else {
                    _error.value = response.body()?.message ?: "Không thể tải chi tiết bài viết"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối mạng: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 4. Gửi bình luận mới vào bài viết
    fun addComment(requestId: String, content: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.addComment(requestId, content)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Thành công -> Gọi lại API lấy chi tiết để cập nhật list bình luận mới nhất
                    fetchRequestDetail(requestId)
                    onSuccess()
                } else {
                    _error.value = response.body()?.message ?: "Lỗi khi gửi bình luận"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi mạng: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 5. Upvote bài viết
    fun upvoteRequest(requestId: String) {
        viewModelScope.launch {
            try {
                val response = repository.upvoteRequest(requestId)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchRequests() // Load lại danh sách bên ngoài

                    // Nếu đang mở đúng bài này ở màn chi tiết thì load lại cả chi tiết
                    if (_selectedRequest.value?.id == requestId) {
                        fetchRequestDetail(requestId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Lỗi mạng: ${e.message}"
            }
        }
    }

    // 6. Đóng bài viết (Đánh dấu đã được giải quyết kèm Link)
    fun resolveRequest(requestId: String, resolvedLink: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.resolveRequest(requestId, resolvedLink)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchRequests()
                    fetchRequestDetail(requestId)
                    onSuccess()
                } else {
                    _error.value = response.body()?.message ?: "Lỗi xử lý yêu cầu"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối mạng: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =============================================================
    // HÀM XỬ LÝ NHẮN TIN RIÊNG
    // =============================================================
    fun getOrCreateChat(requestId: String, receiverId: String, onNavigateToChat: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chatRepository.getOrCreateConversation(requestId, receiverId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val conversationId = response.body()?.data?.id ?: ""
                    if (conversationId.isNotBlank()) {
                        onNavigateToChat(conversationId)
                    } else {
                        _error.value = "Lỗi: Không lấy được ID phòng chat"
                    }
                } else {
                    _error.value = "Không thể mở phòng chat"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối mạng: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}