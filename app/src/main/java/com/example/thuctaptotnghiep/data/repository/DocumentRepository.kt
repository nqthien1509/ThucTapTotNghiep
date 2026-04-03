package com.example.thuctaptotnghiep.data.repository

import com.example.thuctaptotnghiep.data.remote.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

// Hilt sẽ tự động tìm và tiêm ApiService vào đây
class DocumentRepository @Inject constructor(
    private val apiService: ApiService
) {
    // Hàm này sẽ được ViewModel gọi để thực hiện đẩy dữ liệu
    suspend fun uploadDocument(
        title: RequestBody,
        description: RequestBody,
        price: RequestBody,
        file: MultipartBody.Part
    ) {
        // Tạm thời gọi apiService, sau này có Backend ta sẽ xử lý kết quả trả về (thành công/thất bại) tại đây
        apiService.uploadDocument(title, description, price, file)
    }
}