package com.example.thuctaptotnghiep.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.data.network.RetrofitClient
import com.example.thuctaptotnghiep.utils.UserManager // <-- CẬP NHẬT: Import Trạm trung chuyển
import com.google.firebase.auth.FirebaseAuth
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

class ProfileViewModel : ViewModel() {

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
    // CẬP NHẬT: Lấy State trực tiếp từ UserManager (Trạm trung chuyển)
    // =======================================================
    val userProfile: StateFlow<User?> = UserManager.userProfile

    // =======================================================
    // 3. LẤY THÔNG TIN USER TỪ FIREBASE (Làm dữ liệu mặc định)
    // =======================================================
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val rawName = currentUser?.displayName

    // Tên dùng để lấy "Tài liệu của tôi"
    val userName = if (rawName.isNullOrBlank()) "Người dùng ẩn danh" else rawName
    // ID dùng để lấy "Yêu thích" và "Xem sau"
    val userId = currentUser?.uid ?: "default_id"

    init {
        loadAllProfileData()
        fetchUserProfile(userId) // Gọi thêm hàm lấy thông tin User khi khởi tạo
    }

    // Hàm load dữ liệu lần đầu (Sử dụng vòng xoay lớn giữa màn hình)
    fun loadAllProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // TỐI ƯU: Gọi 3 API song song bằng async để tiết kiệm thời gian chờ
                val myDocsDeferred = async { RetrofitClient.apiService.getMyDocuments(userName) }
                val favDocsDeferred = async { RetrofitClient.apiService.getFavoriteDocuments(userId) }
                val watchDocsDeferred = async { RetrofitClient.apiService.getWatchLaterDocuments(userId) }

                // await() đợi cả 3 trả về kết quả
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

    // Hàm tải lại dữ liệu khi vuốt (Pull-to-refresh)
    fun refreshDocuments() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            try {
                val myDocsDeferred = async { RetrofitClient.apiService.getMyDocuments(userName) }
                val favDocsDeferred = async { RetrofitClient.apiService.getFavoriteDocuments(userId) }
                val watchDocsDeferred = async { RetrofitClient.apiService.getWatchLaterDocuments(userId) }

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
                RetrofitClient.apiService.deleteDocument(id)
                _deleteStatus.value = "Đã xóa thành công!"
                loadAllProfileData() // Tải lại toàn bộ danh sách sau khi xóa
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi xóa: ${e.message}"
            }
        }
    }

    fun resetDeleteStatus() {
        _deleteStatus.value = null
    }

    // =======================================================
    // 4. TÍNH NĂNG TÀI KHOẢN (ĐĂNG XUẤT & ĐỔI MẬT KHẨU)
    // =======================================================

    fun logout(onSuccess: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        UserManager.clearUser() // <-- CẬP NHẬT: Xóa dữ liệu User toàn cục khi đăng xuất
        onSuccess()
    }

    fun changePassword(newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onError(task.exception?.message ?: "Có lỗi xảy ra khi đổi mật khẩu")
                    }
                }
        } else {
            onError("Không tìm thấy thông tin người dùng")
        }
    }

    // =======================================================
    // 5. CẬP NHẬT MỚI: QUẢN LÝ THÔNG TIN CÁ NHÂN (MONGODB)
    // =======================================================

    fun fetchUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val user = RetrofitClient.apiService.getUserProfile(uid)
                UserManager.setUser(user) // <-- CẬP NHẬT: Lưu vào Trạm trung chuyển
            } catch (e: Exception) {
                // Lỗi mạng hoặc tài khoản mới tinh chưa có data, im lặng bỏ qua để dùng thông tin Firebase
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
                val updatedUser = RetrofitClient.apiService.updateUserProfile(uid, updateData)
                UserManager.setUser(updatedUser) // <-- CẬP NHẬT: Lưu vào Trạm trung chuyển
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi cập nhật thông tin: ${e.message}"
            }
        }
    }

    // ==========================================
    // CẬP NHẬT: UPLOAD ẢNH ĐẠI DIỆN
    // ==========================================
    fun uploadAvatar(context: Context, uid: String, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Chuyển Uri thành File thực tế
                val file = uriToFile(context, uri)
                if (file != null) {
                    // 2. Ép kiểu File để gửi qua mạng
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("avatar", file.name, requestFile)

                    // 3. Gọi API
                    val updatedUser = RetrofitClient.apiService.uploadAvatar(uid, body)

                    // 4. Cập nhật lại UI thông qua Trạm trung chuyển
                    UserManager.setUser(updatedUser) // <-- CẬP NHẬT
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi tải ảnh lên: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Hàm phụ trợ: Chuyển Uri thành File
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