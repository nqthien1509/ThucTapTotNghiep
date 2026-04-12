package com.example.thuctaptotnghiep.data.repository

import com.example.thuctaptotnghiep.data.network.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class DocumentRepository @Inject constructor(
    private val apiService: ApiService
) {
    // CẬP NHẬT: Thêm 4 tham số mới để đồng bộ với ApiService và ViewModel
    suspend fun uploadDocument(
        file: MultipartBody.Part,
        title: RequestBody,
        authorName: RequestBody,
        subject: RequestBody,
        category: RequestBody,
        description: RequestBody,
        tags: RequestBody
    ): Any {
        // Gọi API và truyền đúng thứ tự các tham số
        return apiService.uploadDocument(
            file = file,
            title = title,
            authorName = authorName,
            subject = subject,
            category = category,
            description = description,
            tags = tags
        )
    }
}