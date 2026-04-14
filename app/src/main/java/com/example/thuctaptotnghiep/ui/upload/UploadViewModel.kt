package com.example.thuctaptotnghiep.ui.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.network.ApiService // Đảm bảo import đúng ApiService của bạn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

// Định nghĩa các trạng thái của màn hình
sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}

@HiltViewModel // Đánh dấu đây là ViewModel của Hilt
class UploadViewModel @Inject constructor(
    private val apiService: ApiService // Hilt tự động bơm ApiService vào đây, không cần gọi RetrofitClient nữa!
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

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
            _uploadState.value = UploadState.Loading

            try {
                // Chuyển URI thành File ngầm
                val file = uriToFile(context, uri)

                if (file != null) {
                    val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                    val authorBody = authorName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val subjectBody = subject.toRequestBody("text/plain".toMediaTypeOrNull())
                    val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val tagsBody = tags.toRequestBody("text/plain".toMediaTypeOrNull())

                    // Gọi thẳng API
                    apiService.uploadDocument(
                        file = filePart, title = titleBody, authorName = authorBody,
                        subject = subjectBody, category = categoryBody, description = descBody, tags = tagsBody
                    )

                    _uploadState.value = UploadState.Success
                    file.delete() // Xóa file tạm sau khi upload xong
                } else {
                    _uploadState.value = UploadState.Error("Không thể đọc được file PDF.")
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Lỗi Server: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uploadState.value = UploadState.Idle
    }

    // Hàm tiện ích nội bộ chuyển URI sang File
    private suspend fun uriToFile(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.pdf")
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            return@withContext tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}