package com.example.thuctaptotnghiep.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.data.repository.DocumentRepository
import com.example.thuctaptotnghiep.utils.UserManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    // CẢI TIẾN 1: Cờ Cache cục bộ
    private var isDataLoaded = false

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

    // CẢI TIẾN 3: Quản lý trạng thái Chờ Xóa (Undo)
    private val deleteJobs = mutableMapOf<String, Job>()
    private val _pendingDeleteIds = MutableStateFlow<Set<String>>(emptySet())
    val pendingDeleteIds: StateFlow<Set<String>> = _pendingDeleteIds.asStateFlow()

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
    // 4. CÁC HÀM XỬ LÝ DỮ LIỆU
    // =======================================================

    fun loadAllProfileData(forceRefresh: Boolean = false) {
        // Áp dụng Cache: Nếu đã có data và không bắt buộc refresh thì bỏ qua
        if (isDataLoaded && !forceRefresh) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Tận dụng async để gọi 3 API song song
                val myDocsDeferred = async { repository.getMyDocuments() }
                val favDocsDeferred = async { repository.getFavoriteDocuments(userId) }
                val watchDocsDeferred = async { repository.getWatchLaterDocuments(userId) }

                _myDocuments.value = myDocsDeferred.await()
                _favoriteDocuments.value = favDocsDeferred.await()
                _watchLaterDocuments.value = watchDocsDeferred.await()

                isDataLoaded = true // Đánh dấu đã load xong cache
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
            loadAllProfileData(forceRefresh = true) // Ép tải lại
            _isRefreshing.value = false
        }
    }

    // =======================================================
    // 5. CÁC HÀM XỬ LÝ XÓA & HOÀN TÁC (OPTIMISTIC UI)
    // =======================================================

    fun deleteDocumentWithUndo(id: String) {
        // Đưa item vào danh sách chờ xóa để UI ẩn nó đi ngay lập tức
        _pendingDeleteIds.value += id

        // Hủy job cũ nếu spam click
        deleteJobs[id]?.cancel()

        deleteJobs[id] = viewModelScope.launch {
            delay(4000) // Cho người dùng 4 giây để bấm "Hoàn tác"

            try {
                // Thực sự gọi API xóa sau khi hết 4 giây
                repository.deleteDocument(id)

                // Cập nhật lại danh sách gốc
                _myDocuments.value = _myDocuments.value.filter { it.id != id }
                _deleteStatus.value = "Đã xóa thành công!"
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi xóa: ${e.message}"
            } finally {
                // Dọn dẹp trạng thái chờ
                _pendingDeleteIds.value -= id
                deleteJobs.remove(id)
            }
        }
    }

    fun undoDelete(id: String) {
        deleteJobs[id]?.cancel() // Dừng lệnh xóa API
        deleteJobs.remove(id)
        _pendingDeleteIds.value -= id // Bỏ khỏi danh sách ẩn, item hiện lại bình thường
    }

    fun resetDeleteStatus() {
        _deleteStatus.value = null
    }

    // =======================================================
    // 6. TÍNH NĂNG TÀI KHOẢN (AUTH & MONGODB)
    // =======================================================

    fun logout(onSuccess: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        UserManager.clearUser()
        onSuccess()
    }

    // CẢI TIẾN 2: Re-Auth trước khi đổi mật khẩu
    fun changePassword(currentPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.email != null) {
            viewModelScope.launch {
                try {
                    // Bước 1: Xác thực lại bằng mật khẩu hiện tại
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
                    user.reauthenticate(credential).await()

                    // Bước 2: Cập nhật mật khẩu mới
                    user.updatePassword(newPass).await()
                    onSuccess()
                } catch (e: Exception) {
                    onError(e.message ?: "Mật khẩu hiện tại không đúng hoặc phiên đăng nhập đã hết hạn!")
                }
            }
        } else {
            onError("Không tìm thấy thông tin xác thực người dùng.")
        }
    }

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