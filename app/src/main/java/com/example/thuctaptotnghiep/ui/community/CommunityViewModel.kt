package com.example.thuctaptotnghiep.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Request
import com.example.thuctaptotnghiep.data.repository.CommunityRepository
import com.example.thuctaptotnghiep.data.repository.ChatRepository // [CẬP NHẬT]: Import ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository,
    private val chatRepository: ChatRepository // [CẬP NHẬT]: Tiêm ChatRepository vào đây
) : ViewModel() {

    // Danh sách bài đăng
    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests.asStateFlow()

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
    // [MỚI]: HÀM XỬ LÝ KHI BẤM NÚT "TRẢ LỜI"
    // =============================================================
    fun getOrCreateChat(requestId: String, receiverId: String, onNavigateToChat: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Gọi API lấy hoặc tạo phòng chat với người đăng bài
                val response = chatRepository.getOrCreateConversation(requestId, receiverId)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Lấy ID phòng chat trả về (nếu trong Model của bạn lưu là _id thì đổi .id thành ._id nhé)
                    val conversationId = response.body()?.data?.id ?: ""
                    if (conversationId.isNotBlank()) {
                        onNavigateToChat(conversationId) // Thành công thì chuyển sang màn hình Chat
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