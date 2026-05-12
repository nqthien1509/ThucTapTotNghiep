package com.example.thuctaptotnghiep.data.repository

import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.UploadResponse
import com.example.thuctaptotnghiep.data.model.User
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
    // 1. TÀI LIỆU CHUNG (Dành cho Home & Upload)
    // ==========================================

    suspend fun getAllDocuments(): List<Document> {
        return apiService.getDocuments()
    }

    suspend fun uploadDocument(
        file: MultipartBody.Part,
        title: RequestBody,
        authorName: RequestBody,
        subject: RequestBody,
        category: RequestBody,
        description: RequestBody,
        tags: RequestBody
    ): UploadResponse {
        return apiService.uploadDocument(
            file, title, authorName, subject, category, description, tags
        )
    }

    // ==========================================
    // 2. TÌM KIẾM (Dành cho SearchViewModel)
    // ==========================================
    suspend fun searchDocuments(keyword: String, category: String?): List<Document> {
        return apiService.searchDocuments(keyword, category)
    }

    // ==========================================
    // 3. CHI TIẾT & TƯƠNG TÁC (Dành cho DetailViewModel)
    // ==========================================

    suspend fun getDocumentById(id: String): Document {
        return apiService.getDocumentById(id)
    }

    suspend fun toggleFavorite(documentId: String) {
        apiService.toggleFavorite(documentId)
    }

    suspend fun toggleWatchLater(documentId: String) {
        apiService.toggleWatchLater(documentId)
    }

    // [MỚI THÊM]: Gọi API tăng lượt xem
    suspend fun incrementView(documentId: String) {
        apiService.incrementView(documentId)
    }

    // [MỚI THÊM]: Gọi API tăng lượt tải
    suspend fun incrementDownload(documentId: String) {
        apiService.incrementDownload(documentId)
    }

    // ==========================================
    // 4. DANH SÁCH CÁ NHÂN & QUẢN LÝ (Dành cho ProfileViewModel)
    // ==========================================

    suspend fun getMyDocuments(): List<Document> {
        return apiService.getMyDocuments()
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
    // 5. THÔNG TIN NGƯỜI DÙNG (Dành cho ProfileViewModel)
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
}