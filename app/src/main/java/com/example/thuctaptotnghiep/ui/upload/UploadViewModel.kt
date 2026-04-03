package com.example.thuctaptotnghiep.ui.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.repository.DocumentRepository
import com.example.thuctaptotnghiep.utils.FileUtils // Import FileUtils bạn vừa tạo
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
    private val repository: DocumentRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri.asStateFlow()

    fun updateTitle(newTitle: String) { _title.value = newTitle }
    fun updateDescription(newDesc: String) { _description.value = newDesc }
    fun updatePrice(newPrice: String) { _price.value = newPrice }
    fun onFileSelected(uri: Uri?) { _selectedFileUri.value = uri }

    // Nhận thêm Context từ màn hình truyền vào để xử lý File
    fun submitDocument(context: Context) {
        viewModelScope.launch {
            try {
                println(" Bắt đầu quá trình chuẩn bị dữ liệu...")
                val currentUri = _selectedFileUri.value

                if (currentUri != null) {
                    // 1. Chuyển Uri (đường dẫn ảo) thành File thực tế thông qua FileUtils
                    val file = FileUtils.uriToFile(context, currentUri)

                    if (file != null) {
                        // 2. Ép kiểu File thành dạng MultipartBody.Part (Chuyên dùng để gửi File qua mạng)
                        val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                        // "file" ở đây là tên của trường dữ liệu mà Backend yêu cầu (phải khớp với API)
                        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                        // 3. Ép kiểu các Text (Title, Description, Price) thành RequestBody
                        val titleBody = _title.value.toRequestBody("text/plain".toMediaTypeOrNull())
                        val descBody = _description.value.toRequestBody("text/plain".toMediaTypeOrNull())
                        val priceBody = _price.value.toRequestBody("text/plain".toMediaTypeOrNull())

                        println(" Chuyển đổi File thành công! Nằm tại: ${file.absolutePath}")
                        println(" Đã đóng gói xong dữ liệu Text.")

                        // 4. GỌI API (Đang tạm comment chờ Backend hoàn thiện)
                        // repository.uploadDocument(titleBody, descBody, priceBody, filePart)

                        println(" Giả lập Upload thành công lên Server!")
                    } else {
                        println(" Lỗi: Không thể tạo file từ Uri (Có thể do thiếu quyền hoặc file bị lỗi)")
                    }
                }
            } catch (e: Exception) {
                println(" Có lỗi xảy ra trong quá trình Upload: ${e.message}")
            }
        }
    }
}