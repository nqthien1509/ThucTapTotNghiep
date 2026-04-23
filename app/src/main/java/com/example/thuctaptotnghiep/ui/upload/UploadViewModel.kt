package com.example.thuctaptotnghiep.ui.upload

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job // Bổ sung import Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

// CẢI TIẾN 1: Thay đổi Success để mang theo documentId phục vụ điều hướng
sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val documentId: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    // CẢI TIẾN 2: StateFlow quản lý % tiến trình upload
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private var progressJob: Job? = null

    // CẢI TIẾN 3: Validate chống ký tự xấu, script độc hại (Bảo mật)
    private fun isValidText(text: String): Boolean {
        // Chỉ cho phép chữ, số, tiếng Việt, khoảng trắng và các dấu phẩy, chấm, gạch ngang
        val safePattern = "^[a-zA-Z0-9_ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ\\s\\-\\.,]+$"
        return text.matches(safePattern.toRegex())
    }

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
        // Ràng buộc Validate
        if (title.isBlank() || subject.isBlank() || tags.isBlank()) {
            _uploadState.value = UploadState.Error("Vui lòng nhập đầy đủ các trường thông tin!")
            return
        }
        if (!isValidText(title) || !isValidText(subject) || !isValidText(tags)) {
            _uploadState.value = UploadState.Error("Thông tin chứa ký tự đặc biệt không hợp lệ!")
            return
        }

        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            startSimulatedProgress() // Kích hoạt hiệu ứng chạy phần trăm

            try {
                val file = uriToFile(context, uri)

                if (file != null) {
                    if (file.length() == 0L) {
                        file.delete()
                        stopSimulatedProgress()
                        _uploadState.value = UploadState.Error("File bị rỗng (0 bytes). Vui lòng kiểm tra lại file!")
                        return@launch
                    }

                    val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                    val authorBody = authorName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val subjectBody = subject.toRequestBody("text/plain".toMediaTypeOrNull())
                    val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val tagsBody = tags.toRequestBody("text/plain".toMediaTypeOrNull())

                    // Gọi API thông qua Repository
                    // Giả định hàm này thành công sẽ không quăng Exception
                    val result = repository.uploadDocument(
                        file = filePart, title = titleBody, authorName = authorBody,
                        subject = subjectBody, category = categoryBody, description = descBody, tags = tagsBody
                    )

                    stopSimulatedProgress() // Đẩy thanh upload lên 100%

                    // Tương lai: Nếu backend trả về Document Object, bạn lấy result.id
                    // Tạm thời tạo mock ID để UI hiển thị được nút CTA Xem tài liệu
                    val mockNewDocumentId = "DOC_${System.currentTimeMillis()}"

                    _uploadState.value = UploadState.Success(documentId = mockNewDocumentId)
                    file.delete()
                } else {
                    stopSimulatedProgress()
                    _uploadState.value = UploadState.Error("Không thể đọc được file PDF từ thiết bị.")
                }
            } catch (e: Exception) {
                stopSimulatedProgress()
                if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("UploadError", "Lỗi từ Backend: $errorBody")

                    val errorMessage = try {
                        val jsonObject = JSONObject(errorBody ?: "")
                        jsonObject.getString("message")
                    } catch (jsonEx: Exception) {
                        errorBody ?: "Lỗi xác thực dữ liệu đầu vào (400)"
                    }
                    _uploadState.value = UploadState.Error(errorMessage)
                } else {
                    _uploadState.value = UploadState.Error("Lỗi kết nối: ${e.message}")
                }
            }
        }
    }

    fun resetState() {
        progressJob?.cancel()
        _uploadProgress.value = 0f
        _uploadState.value = UploadState.Idle
    }

    // --- UX Logic: Giả lập thanh phần trăm tải lên ---
    private fun startSimulatedProgress() {
        _uploadProgress.value = 0f
        progressJob = viewModelScope.launch {
            var progress = 0f
            // Chạy tà tà lên 90% rồi dừng đợi server phản hồi
            while (progress < 0.9f) {
                delay(400)
                progress += 0.05f
                _uploadProgress.value = progress
            }
        }
    }

    private fun stopSimulatedProgress() {
        progressJob?.cancel()
        _uploadProgress.value = 1f // Đẩy thẳng lên 100%
    }

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