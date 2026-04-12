package com.example.thuctaptotnghiep.ui.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.network.RetrofitClient // Sử dụng trực tiếp RetrofitClient
// import com.example.thuctaptotnghiep.data.repository.DocumentRepository // (Mở comment nếu bạn dùng Repository)
import com.example.thuctaptotnghiep.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    // private val repository: DocumentRepository // Mở comment nếu bạn muốn gọi qua Repository
) : ViewModel() {

    // ==========================================
    // STATE QUẢN LÝ GIAO DIỆN TỪ VIEWMODEL
    // ==========================================
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ==========================================
    // HÀM XỬ LÝ UPLOAD CHÍNH
    // ==========================================
    fun uploadDocument(
        context: Context,
        uri: Uri,
        title: String,
        authorName: String,
        subject: String,
        category: String,
        description: String,
        tags: String
    ) {
        viewModelScope.launch {
            // Đặt trạng thái bắt đầu tải lên
            _isUploading.value = true
            _errorMessage.value = null
            _uploadSuccess.value = false

            try {
                // 1. Chuyển Uri thành File thực tế
                val file = FileUtils.uriToFile(context, uri)

                if (file != null) {
                    // 2. Ép kiểu File thành dạng MultipartBody.Part (Chuyên dùng gửi File)
                    val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    // 3. Ép kiểu các Text thành RequestBody
                    val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                    val authorBody = authorName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val subjectBody = subject.toRequestBody("text/plain".toMediaTypeOrNull())
                    val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val tagsBody = tags.toRequestBody("text/plain".toMediaTypeOrNull())

                    // 4. GỌI API (Nếu bạn thiết lập Repository thì thay RetrofitClient bằng repository.uploadDocument)
                    RetrofitClient.apiService.uploadDocument(
                        file = filePart,
                        title = titleBody,
                        authorName = authorBody,
                        subject = subjectBody,
                        category = categoryBody,
                        description = descBody,
                        tags = tagsBody
                    )

                    // Nếu API chạy không ném ra lỗi (catch) thì tức là thành công
                    _uploadSuccess.value = true

                } else {
                    _errorMessage.value = "Không thể đọc được file PDF từ thiết bị của bạn."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi kết nối Server: ${e.message}"
            } finally {
                // Tắt vòng xoay Loading dù thành công hay thất bại
                _isUploading.value = false
            }
        }
    }

    // Hàm reset trạng thái sau khi hiển thị Toast thành công/thất bại
    fun resetState() {
        _uploadSuccess.value = false
        _errorMessage.value = null
    }
}