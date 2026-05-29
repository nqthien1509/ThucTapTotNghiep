package com.example.thuctaptotnghiep.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    // =======================================================
    // [CẬP NHẬT CỐT LÕI]: SỬ DỤNG CHANNEL CHO NAVIGATION EVENT
    // Channel đảm bảo sự kiện chỉ phát 1 lần và không bao giờ bị kẹt
    // =======================================================
    private val _authSuccessEvent = Channel<Unit>(Channel.BUFFERED)
    val authSuccessEvent = _authSuccessEvent.receiveAsFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authMessage.value = "Vui lòng nhập đầy đủ Email và Mật khẩu!"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authMessage.value = "Định dạng email không hợp lệ!"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _authMessage.value = "Đăng nhập thành công!"

                // [CẬP NHẬT]: Bắn sự kiện chuyển trang
                _authSuccessEvent.send(Unit)
            } catch (e: FirebaseAuthInvalidUserException) {
                _authMessage.value = "Tài khoản chưa được đăng ký!"
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authMessage.value = "Sai email hoặc mật khẩu!"
            } catch (e: FirebaseNetworkException) {
                _authMessage.value = "Lỗi kết nối mạng. Vui lòng kiểm tra Internet!"
            } catch (e: Exception) {
                _authMessage.value = "Đăng nhập thất bại. Vui lòng thử lại sau!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, pass: String, confirmPass: String, isTermsAccepted: Boolean) {
        _nameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null

        var isValid = true

        if (name.isEmpty()) { _nameError.value = "Họ tên không được để trống"; isValid = false }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { _emailError.value = "Email không hợp lệ"; isValid = false }
        val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#\$%^&+=!]).{8,}\$"
        if (!pass.matches(passwordPattern.toRegex())) { _passwordError.value = "Mật khẩu cần ≥ 8 ký tự, gồm chữ hoa, số và ký tự đặc biệt"; isValid = false }
        if (pass != confirmPass) { _confirmPasswordError.value = "Mật khẩu xác nhận không khớp"; isValid = false }
        if (!isTermsAccepted) { _authMessage.value = "Bạn cần đồng ý với Điều khoản và Chính sách!"; isValid = false }

        if (!isValid) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user?.updateProfile(profileUpdates)?.await()

                _authMessage.value = "Đăng ký thành công!"

                // [CẬP NHẬT]: Bắn sự kiện chuyển trang
                _authSuccessEvent.send(Unit)
            } catch (e: FirebaseAuthUserCollisionException) {
                _emailError.value = "Email này đã được đăng ký!"
            } catch (e: Exception) {
                _authMessage.value = "Lỗi đăng ký. Vui lòng thử lại sau!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetMessage() {
        _authMessage.value = null
    }
}