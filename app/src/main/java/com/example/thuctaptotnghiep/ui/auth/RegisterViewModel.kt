package com.example.thuctaptotnghiep.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun updateName(newName: String) { _name.value = newName }
    fun updateEmail(newEmail: String) { _email.value = newEmail }
    fun updatePassword(newPassword: String) { _password.value = newPassword }
    fun updateConfirmPassword(newConfirm: String) { _confirmPassword.value = newConfirm }
    fun resetState() { _registerState.value = RegisterState.Idle }

    fun register() {
        if (_name.value.isBlank() || _email.value.isBlank() || _password.value.isBlank()) {
            _registerState.value = RegisterState.Error("Vui lòng nhập đầy đủ thông tin!")
            return
        }

        if (_password.value != _confirmPassword.value) {
            _registerState.value = RegisterState.Error("Mật khẩu xác nhận không khớp!")
            return
        }

        if (_password.value.length < 6) {
            _registerState.value = RegisterState.Error("Mật khẩu phải có ít nhất 6 ký tự!")
            return
        }

        _registerState.value = RegisterState.Loading

        // Gọi Firebase tạo tài khoản mới
        auth.createUserWithEmailAndPassword(_email.value, _password.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // TODO: Sau này có thể lưu thêm _name.value vào Firestore hoặc Realtime Database
                    _registerState.value = RegisterState.Success
                } else {
                    _registerState.value = RegisterState.Error(task.exception?.message ?: "Đăng ký thất bại")
                }
            }
    }
}