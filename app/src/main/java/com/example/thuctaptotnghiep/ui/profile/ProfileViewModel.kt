package com.example.thuctaptotnghiep.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.data.repository.DocumentRepository // Gọi Repository
import com.example.thuctaptotnghiep.utils.UserManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel // Bắt buộc cho Hilt
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel // Khai báo ViewModel được Hilt quản lý
class ProfileViewModel @Inject constructor(
    private val repository: DocumentRepository // TIÊM REPOSITORY VÀO ĐÂY
) : ViewModel() {

    // =======================================================
    // 1. STATE QUẢN LÝ 3 DANH SÁCH TÀI LIỆU
    // =======================================================
    private val _myDocuments = MutableStateFlow<List<Document>>(emptyList())
    val myDocuments: StateFlow<List<Document>> = _myDocuments.asStateFlow()

    private val _favoriteDocuments = MutableStateFlow<List<Document>>(emptyList())
    val favoriteDocuments: StateFlow<List<Document>> = _favoriteDocuments.asStateFlow()

    private val _watchLaterDocuments = MutableStateFlow<List<Document>>(emptyList())
    val watchLaterDocuments: StateFlow<List<Document>> = _watchLaterDocuments.asStateFlow()

    // =======================================================
    // 2. STATE QUẢN LÝ GIAO DIỆN (LOADING, LỖI, XÓA)
    // =======================================================
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _deleteStatus = MutableStateFlow<String?>(null)
    val deleteStatus: StateFlow<String?> = _deleteStatus.asStateFlow()

    // =======================================================
    // 3. TRẠM TRUNG CHUYỂN DỮ LIỆU USER
    // =======================================================
    val userProfile: StateFlow<User?> = UserManager.userProfile

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val rawName = currentUser?.displayName

    val userName = if (rawName.isNullOrBlank()) "Người dùng ẩn danh" else rawName
    val userId = currentUser?.uid ?: "default_id"

    init {
        loadAllProfileData()
        fetchUserProfile(userId)
    }

    // =======================================================
    // 4. CÁC HÀM XỬ LÝ (SỬ DỤNG REPOSITORY THAY VÌ RETROFITCLIENT)
    // =======================================================

    fun loadAllProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val myDocsDeferred = async { repository.getMyDocuments(userName) }
                val favDocsDeferred = async { repository.getFavoriteDocuments(userId) }
                val watchDocsDeferred = async { repository.getWatchLaterDocuments(userId) }

                _myDocuments.value = myDocsDeferred.await()
                _favoriteDocuments.value = favDocsDeferred.await()
                _watchLaterDocuments.value = watchDocsDeferred.await()

            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshDocuments() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            try {
                val myDocsDeferred = async { repository.getMyDocuments(userName) }
                val favDocsDeferred = async { repository.getFavoriteDocuments(userId) }
                val watchDocsDeferred = async { repository.getWatchLaterDocuments(userId) }

                _myDocuments.value = myDocsDeferred.await()
                _favoriteDocuments.value = favDocsDeferred.await()
                _watchLaterDocuments.value = watchDocsDeferred.await()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteDocument(id)
                _deleteStatus.value = "Đã xóa thành công!"
                loadAllProfileData()
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi xóa: ${e.message}"
            }
        }
    }

    fun resetDeleteStatus() {
        _deleteStatus.value = null
    }

    // TÍNH NĂNG TÀI KHOẢN
    fun logout(onSuccess: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        UserManager.clearUser()
        onSuccess()
    }

    fun changePassword(newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) onSuccess()
                    else onError(task.exception?.message ?: "Có lỗi xảy ra khi đổi mật khẩu")
                }
        } else {
            onError("Không tìm thấy thông tin người dùng")
        }
    }

    // QUẢN LÝ THÔNG TIN CÁ NHÂN (MONGODB)
    fun fetchUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUserProfile(uid)
                UserManager.setUser(user)
            } catch (e: Exception) {
                // Bỏ qua nếu lỗi (tài khoản mới chưa có data)
            }
        }
    }

    fun updateProfile(uid: String, email: String, name: String, school: String, bio: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val updateData = mapOf(
                    "email" to email,
                    "displayName" to name,
                    "school" to school,
                    "bio" to bio
                )
                val updatedUser = repository.updateUserProfile(uid, updateData)
                UserManager.setUser(updatedUser)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi cập nhật thông tin: ${e.message}"
            }
        }
    }

    fun uploadAvatar(context: Context, uid: String, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val file = uriToFile(context, uri)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("avatar", file.name, requestFile)

                    val updatedUser = repository.uploadAvatar(uid, body)
                    UserManager.setUser(updatedUser)
                    file.delete() // Dọn rác
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi tải ảnh lên: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Hàm phụ trợ
    private fun uriToFile(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val tempFile = File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}