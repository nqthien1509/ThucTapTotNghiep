package com.example.thuctaptotnghiep.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.AppNotification
import com.example.thuctaptotnghiep.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel // Import annotation của Hilt
import javax.inject.Inject // Import Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel // Báo cho Hilt biết đây là ViewModel cần được nó quản lý
class NotificationViewModel @Inject constructor( // Nhờ Hilt "bơm" ApiService vào đây
    private val apiService: ApiService
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _notifications.value = apiService.getNotifications()
            } catch (e: Exception) {
                // Bạn có thể log lỗi ra đây nếu cần debug: Log.e("NotificationVM", "Lỗi lấy thông báo", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                // Gọi API đánh dấu đã đọc
                apiService.markAllAsRead()
                // Sau đó tải lại danh sách để cập nhật giao diện (mất màu nền xanh của thông báo mới)
                fetchNotifications()
            } catch (e: Exception) {
                // Bỏ qua lỗi hoặc log ra
            }
        }
    }
}