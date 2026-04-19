package com.example.thuctaptotnghiep.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _isAuthSuccess = MutableStateFlow(false)
    val isAuthSuccess: StateFlow<Boolean> = _isAuthSuccess.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authMessage.value = "Vui lòng nhập đầy đủ Email và Mật khẩu!"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _authMessage.value = "Đăng nhập thành công!"
                _isAuthSuccess.value = true
            } catch (e: Exception) {
                _authMessage.value = "Sai email hoặc mật khẩu! Vui lòng thử lại."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, pass: String, confirmPass: String) {
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            _authMessage.value = "Vui lòng nhập đầy đủ thông tin!"
            return
        }
        if (pass != confirmPass) {
            _authMessage.value = "Mật khẩu xác nhận không khớp!"
            return
        }
        if (pass.length < 6) {
            _authMessage.value = "Mật khẩu phải từ 6 ký tự trở lên!"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Tạo tài khoản trên Firebase
                val result = auth.createUserWithEmailAndPassword(email, pass).await()

                // 2. Cập nhật Tên hiển thị (Display Name) vào hồ sơ Firebase
                val user = result.user
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user?.updateProfile(profileUpdates)?.await()

                _authMessage.value = "Đăng ký thành công!"
                _isAuthSuccess.value = true
            } catch (e: Exception) {
                _authMessage.value = "Lỗi đăng ký: Tài khoản đã tồn tại hoặc email sai định dạng."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetMessage() {
        _authMessage.value = null
    }
}