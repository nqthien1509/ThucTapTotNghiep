package com.example.thuctaptotnghiep.data.network

import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.UploadResponse // <-- Import thêm UploadResponse
import com.example.thuctaptotnghiep.data.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ==========================================
    // 1. TÀI LIỆU (DOCUMENTS)
    // ==========================================

    // CẢI TIẾN QUAN TRỌNG: Đổi thành UploadResponse để khớp với định dạng JSON { message, document }
    @Multipart
    @POST("api/upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("authorName") authorName: RequestBody,
        @Part("subject") subject: RequestBody,
        @Part("category") category: RequestBody,
        @Part("description") description: RequestBody,
        @Part("tags") tags: RequestBody
    ): UploadResponse

    @GET("api/documents")
    suspend fun getDocuments(): List<Document>

    @GET("api/documents/{id}")
    suspend fun getDocumentById(
        @Path("id") id: String
    ): Document

    @GET("api/search")
    suspend fun searchDocuments(
        @Query("q") keyword: String,
        @Query("category") category: String? = null
    ): List<Document>

    // CẢI TIẾN: Trả về Unit vì chỉ cần biết xóa thành công hay không
    @DELETE("api/documents/{id}")
    suspend fun deleteDocument(@Path("id") id: String): Unit

    // =======================================================
    // 2. TƯƠNG TÁC (FAVORITE / WATCH LATER)
    // =======================================================

    @POST("api/documents/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") id: String): Unit

    @POST("api/documents/{id}/watch-later")
    suspend fun toggleWatchLater(@Path("id") id: String): Unit

    // =======================================================
    // 3. DANH SÁCH CÁ NHÂN
    // =======================================================

    @GET("api/my-documents")
    suspend fun getMyDocuments(): List<Document>

    @GET("api/users/{userId}/favorites")
    suspend fun getFavoriteDocuments(@Path("userId") userId: String): List<Document>

    @GET("api/users/{userId}/watch-later")
    suspend fun getWatchLaterDocuments(@Path("userId") userId: String): List<Document>

    // =======================================================
    // 4. QUẢN LÝ THÔNG TIN USER (PROFILE)
    // =======================================================

    @GET("api/user/{uid}")
    suspend fun getUserProfile(@Path("uid") uid: String): User

    @PUT("api/user/{uid}")
    suspend fun updateUserProfile(
        @Path("uid") uid: String,
        @Body profileData: Map<String, String>
    ): User

    @Multipart
    @POST("api/user/{uid}/avatar")
    suspend fun uploadAvatar(
        @Path("uid") uid: String,
        @Part avatar: MultipartBody.Part
    ): User
}