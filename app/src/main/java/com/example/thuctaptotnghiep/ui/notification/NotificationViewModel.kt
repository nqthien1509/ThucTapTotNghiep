package com.example.thuctaptotnghiep.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.AppNotification
import com.example.thuctaptotnghiep.data.network.ApiService
import com.example.thuctaptotnghiep.utils.NotificationEventBus // <-- Import EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Phục vụ cho phân trang (Pagination)
    private var currentPage = 1
    var isLastPage = false
        private set

    // [CẬP NHẬT TỐI ƯU]: Biến cờ khóa API, chống spam request gây lỗi 429
    private var isFetching = false

    init {
        // Tải danh sách lần đầu
        fetchNotifications(isRefresh = true)

        // Lắng nghe thông báo real-time từ Firebase đẩy về khi app đang mở
        listenToRealTimeNotifications()
    }

    // Hàm lấy thông báo có hỗ trợ Refresh (làm mới) hoặc Load More (tải thêm)
    fun fetchNotifications(isRefresh: Boolean = true) {
        // 1. NẾU ĐANG TẢI RỒI THÌ CHẶN LẠI LUÔN
        if (isFetching) return

        // 2. NẾU ĐÃ HẾT TRANG THÌ KHÔNG TẢI THÊM NỮA
        if (isLastPage && !isRefresh) return

        if (isRefresh) {
            currentPage = 1
            isLastPage = false
            _isLoading.value = true
        }

        // 3. BẮT ĐẦU TẢI -> KHÓA CỬA LẠI
        isFetching = true

        viewModelScope.launch {
            try {
                // Gọi API với page hiện tại, limit = 20
                val newNotifications = apiService.getNotifications(page = currentPage, limit = 20)

                if (newNotifications.isEmpty()) {
                    isLastPage = true // Đã hết dữ liệu trên server
                } else {
                    if (isRefresh) {
                        _notifications.value = newNotifications // Ghi đè danh sách mới
                    } else {
                        // Lọc trùng lặp phòng hờ và nối thêm vào cuối danh sách
                        val currentIds = _notifications.value.map { it._id }
                        val uniqueNewNotifications = newNotifications.filter { it._id !in currentIds }
                        _notifications.value = _notifications.value + uniqueNewNotifications
                    }
                    currentPage++ // Tăng trang lên cho lần tải tiếp theo
                }
            } catch (e: Exception) {
                // [CẬP NHẬT]: In lỗi ra Logcat để kiểm tra nguyên nhân nếu danh sách không tải được
                android.util.Log.e("NotificationVM", "Lỗi lấy thông báo: ${e.message}", e)
            } finally {
                // 4. TẢI XONG (hoặc lỗi) -> MỞ KHÓA RA CHO PHÉP TẢI TIẾP
                isFetching = false
                if (isRefresh) _isLoading.value = false
            }
        }
    }

    // Đánh dấu 1 thông báo là đã đọc
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                apiService.markAsRead(notificationId)
                // Cập nhật local UI ngay lập tức cho mượt
                _notifications.value = _notifications.value.map {
                    if (it._id == notificationId) it.copy(isRead = true) else it
                }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }

    // Xóa 1 thông báo
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                apiService.deleteNotification(notificationId)
                // Xóa khỏi danh sách local để UI cập nhật ngay
                _notifications.value = _notifications.value.filter { it._id != notificationId }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }

    // Đánh dấu đọc tất cả
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                apiService.markAllAsRead()
                // Cập nhật tất cả item local thành isRead = true thay vì gọi lại API cho nhẹ
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }

    // Lắng nghe EventBus khi có push notification lúc app đang mở
    private fun listenToRealTimeNotifications() {
        viewModelScope.launch {
            NotificationEventBus.events.collect { newNotif ->
                val currentList = _notifications.value.toMutableList()
                // [TỐI ƯU]: Kiểm tra để tránh chèn trùng nếu người dùng vừa mới vuốt Refresh cùng lúc
                if (currentList.none { it._id == newNotif._id }) {
                    currentList.add(0, newNotif) // Chèn thông báo mới nhất vào vị trí đầu tiên
                    _notifications.value = currentList
                }
            }
        }
    }
}