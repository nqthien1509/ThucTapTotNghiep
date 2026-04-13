package com.example.thuctaptotnghiep.utils

import com.example.thuctaptotnghiep.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Dùng 'object' thay vì 'class' để biến nó thành Singleton (Duy nhất 1 bản sao tồn tại trong toàn app)
object UserManager {
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    // Hàm để cập nhật dữ liệu mới
    fun setUser(user: User) {
        _userProfile.value = user
    }

    // Hàm để xóa dữ liệu khi Đăng xuất
    fun clearUser() {
        _userProfile.value = null
    }
}