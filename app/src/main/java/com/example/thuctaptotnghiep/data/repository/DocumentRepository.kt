package com.example.thuctaptotnghiep.data.repository

import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.User // Bắt buộc phải import Model User
import com.example.thuctaptotnghiep.data.network.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val apiService: ApiService
) {
    // ==========================================
    // 1. HÀM UPLOAD TÀI LIỆU (Dành cho UploadViewModel)
    // ==========================================
    suspend fun uploadDocument(
        file: MultipartBody.Part,
        title: RequestBody,
        authorName: RequestBody,
        subject: RequestBody,
        category: RequestBody,
        description: RequestBody,
        tags: RequestBody
    ): Any {
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

    // ==========================================
    // 2. HÀM TÌM KIẾM TÀI LIỆU (Dành cho SearchViewModel)
    // ==========================================
    suspend fun searchDocuments(
        keyword: String,
        category: String?
    ): List<Document> {
        return apiService.searchDocuments(
            keyword = keyword,
            category = category
        )
    }

    // ==========================================
    // 3. QUẢN LÝ DANH SÁCH CÁ NHÂN (Dành cho ProfileViewModel)
    // ==========================================
    suspend fun getMyDocuments(authorName: String): List<Document> {
        return apiService.getMyDocuments(authorName)
    }

    suspend fun getFavoriteDocuments(userId: String): List<Document> {
        return apiService.getFavoriteDocuments(userId)
    }

    suspend fun getWatchLaterDocuments(userId: String): List<Document> {
        return apiService.getWatchLaterDocuments(userId)
    }

    suspend fun deleteDocument(id: String) {
        apiService.deleteDocument(id)
    }

    // ==========================================
    // 4. QUẢN LÝ THÔNG TIN NGƯỜI DÙNG (Dành cho ProfileViewModel)
    // ==========================================
    suspend fun getUserProfile(uid: String): User {
        return apiService.getUserProfile(uid)
    }

    suspend fun updateUserProfile(uid: String, data: Map<String, String>): User {
        return apiService.updateUserProfile(uid, data)
    }

    suspend fun uploadAvatar(uid: String, file: MultipartBody.Part): User {
        return apiService.uploadAvatar(uid, file)
    }
    // ==========================================
    // LẤY TẤT CẢ TÀI LIỆU (Dành cho HomeViewModel)
    // ==========================================
    suspend fun getAllDocuments(): List<Document> {
        return apiService.getDocuments()
    }
    // ==========================================
    // 5. CHI TIẾT & TƯƠNG TÁC TÀI LIỆU (Dành cho DocumentDetailViewModel)
    // ==========================================
    suspend fun getDocumentById(id: String, userId: String): Document {
        return apiService.getDocumentById(id, userId)
    }

    suspend fun toggleFavorite(documentId: String, body: Map<String, String>) {
        apiService.toggleFavorite(documentId, body)
    }

    suspend fun toggleWatchLater(documentId: String, body: Map<String, String>) {
        apiService.toggleWatchLater(documentId, body)
    }
}