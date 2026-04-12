package com.example.thuctaptotnghiep.data.repository

import com.example.thuctaptotnghiep.data.network.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class DocumentRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun uploadDocument(
        file: MultipartBody.Part,
        title: RequestBody,
        authorName: RequestBody
    ) {
        // Gọi API với đúng 3 tham số và đúng thứ tự: file -> title -> author
        apiService.uploadDocument(file, title, authorName)
    }
}