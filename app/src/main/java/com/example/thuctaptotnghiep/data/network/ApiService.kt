package com.example.thuctaptotnghiep.data.network

import com.example.thuctaptotnghiep.data.model.Document
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @Multipart
    @POST("api/upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("authorName") authorName: RequestBody
    ): Any

    @GET("api/documents")
    suspend fun getDocuments(): List<Document>

    // Lấy chi tiết tài liệu (kèm trạng thái tương tác của user)
    @GET("api/documents/{id}")
    suspend fun getDocumentById(
        @Path("id") id: String,
        @Query("userId") userId: String? = null
    ): Document

    @GET("api/search")
    suspend fun searchDocuments(@Query("q") keyword: String): List<Document>

    // =======================================================
    // API: TRUY XUẤT DANH SÁCH THEO USER (PROFILE)
    // =======================================================

    // 1. Lấy tài liệu do user tự đăng (Của tôi)
    @GET("api/my-documents/{authorName}")
    suspend fun getMyDocuments(@Path("authorName") authorName: String): List<Document>

    // 2. THÊM MỚI: Lấy danh sách tài liệu đã Yêu thích
    @GET("api/users/{userId}/favorites")
    suspend fun getFavoriteDocuments(@Path("userId") userId: String): List<Document>

    // 3. THÊM MỚI: Lấy danh sách tài liệu đã Xem sau
    @GET("api/users/{userId}/watch-later")
    suspend fun getWatchLaterDocuments(@Path("userId") userId: String): List<Document>

    // =======================================================
    // API: TƯƠNG TÁC & QUẢN LÝ
    // =======================================================

    // Xóa tài liệu
    @DELETE("api/documents/{id}")
    suspend fun deleteDocument(@Path("id") id: String): Any

    // API Toggle Yêu thích
    @POST("api/documents/{id}/favorite")
    suspend fun toggleFavorite(
        @Path("id") id: String,
        @Body requestBody: Map<String, String>
    ): Any

    // API Toggle Xem lại sau
    @POST("api/documents/{id}/watch-later")
    suspend fun toggleWatchLater(
        @Path("id") id: String,
        @Body requestBody: Map<String, String>
    ): Any
}