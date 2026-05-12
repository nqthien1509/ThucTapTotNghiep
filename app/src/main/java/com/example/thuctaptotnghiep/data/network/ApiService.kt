package com.example.thuctaptotnghiep.data.network

import com.example.thuctaptotnghiep.data.model.AppNotification
import com.example.thuctaptotnghiep.data.model.BaseResponse
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.UploadResponse
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.data.model.Request
import com.example.thuctaptotnghiep.data.model.Conversation
import com.example.thuctaptotnghiep.data.model.Message

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
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

    @DELETE("api/documents/{id}")
    suspend fun deleteDocument(@Path("id") id: String): Unit

    @PUT("api/documents/{id}/view")
    suspend fun incrementView(@Path("id") documentId: String)

    @PUT("api/documents/{id}/download")
    suspend fun incrementDownload(@Path("id") documentId: String)

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

    // =======================================================
    // 5. THÔNG BÁO (NOTIFICATIONS)
    // =======================================================

    // Lấy danh sách thông báo của user (Đã tích hợp phân trang)
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<AppNotification>

    // Đánh dấu tất cả thông báo là đã đọc
    @PUT("api/notifications/read-all")
    suspend fun markAllAsRead(): Response<Unit>

    // Đánh dấu MỘT thông báo là đã đọc
    @PUT("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Unit

    // Xóa MỘT thông báo
    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Unit

    // =======================================================
    // 6. CỘNG ĐỒNG (COMMUNITY / REQUESTS) - [MỚI]
    // =======================================================

    // Lấy danh sách bài đăng xin tài liệu
    @GET("api/requests")
    suspend fun getRequests(): Response<BaseResponse<List<Request>>>
    // Ghi chú: Nếu BE trả về { "success": true, "data": [...] }, bạn cần tạo thêm 1 class BaseResponse<T>(val success: Boolean, val data: T).
    // Nếu BE trả về trực tiếp mảng [...] thì đổi thành suspend fun getRequests(): List<Request>

    // Tạo bài đăng xin tài liệu mới
    @POST("api/requests")
    suspend fun createRequest(@Body body: Map<String, String>): Response<BaseResponse<Request>>

    // =======================================================
    // 7. NHẮN TIN (CHAT) - [MỚI]
    // =======================================================

    // Gọi khi bấm "Trả lời", BE sẽ kiểm tra nếu đã có phòng chat thì trả về ID cũ, chưa thì tạo phòng mới
    @POST("api/chat/conversations")
    suspend fun getOrCreateConversation(@Body body: Map<String, String>): Response<BaseResponse<Conversation>>

    // Lấy lịch sử tin nhắn của một phòng chat cụ thể để load lên giao diện
    @GET("api/chat/messages/{conversationId}")
    suspend fun getMessages(@Path("conversationId") conversationId: String): Response<BaseResponse<List<Message>>>

    @GET("api/chat/conversations/me")
    suspend fun getMyConversations(): Response<BaseResponse<List<Conversation>>>
}