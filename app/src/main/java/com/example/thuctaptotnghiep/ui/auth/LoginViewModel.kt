package com.example.thuctaptotnghiep.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Khai báo các trạng thái của màn hình đăng nhập
sealed class LoginState {
    object Idle : LoginState() // Trạng thái nghỉ (chưa làm gì)
    object Loading : LoginState() // Đang xoay vòng chờ Firebase trả lời
    object Success : LoginState() // Đăng nhập thành công
    data class Error(val message: String) : LoginState() // Báo lỗi sai mật khẩu, v.v.
}

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    // Gọi "Thần đèn" Firebase Auth
    private val auth = FirebaseAuth.getInstance()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun updateEmail(newEmail: String) { _email.value = newEmail }
    fun updatePassword(newPassword: String) { _password.value = newPassword }
    fun resetState() { _loginState.value = LoginState.Idle }

    // Hàm thực hiện đăng nhập
    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _loginState.value = LoginState.Error("Vui lòng nhập đầy đủ Email và Mật khẩu!")
            return
        }

        _loginState.value = LoginState.Loading // Hiển thị trạng thái đang tải

        // Lệnh thần thánh: Bắt Firebase kiểm tra tài khoản
        auth.signInWithEmailAndPassword(_email.value, _password.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error(task.exception?.message ?: "Đăng nhập thất bại")
                }
            }
    }
}