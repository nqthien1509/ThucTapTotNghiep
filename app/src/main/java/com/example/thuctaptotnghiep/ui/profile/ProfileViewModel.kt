package com.example.thuctaptotnghiep.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    // 3. LẤY THÔNG TIN USER TỪ FIREBASE
    // =======================================================
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val rawName = currentUser?.displayName

    // Tên dùng để lấy "Tài liệu của tôi"
    val userName = if (rawName.isNullOrBlank()) "Người dùng ẩn danh" else rawName
    // ID dùng để lấy "Yêu thích" và "Xem sau"
    val userId = currentUser?.uid ?: "default_id"

    init {
        loadAllProfileData()
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
}