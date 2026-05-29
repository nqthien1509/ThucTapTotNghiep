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

    @GET("api/documents/leaderboard/top")
    suspend fun getTopDocuments(): Response<BaseResponse<List<Document>>>

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

    @GET("api/user/leaderboard/top")
    suspend fun getTopContributors(): Response<BaseResponse<List<User>>>

    // =======================================================
    // 5. THÔNG BÁO (NOTIFICATIONS)
    // =======================================================

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<AppNotification>

    @PUT("api/notifications/read-all")
    suspend fun markAllAsRead(): Response<Unit>

    @PUT("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Unit

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Unit

    // =======================================================
    // 6. CỘNG ĐỒNG (COMMUNITY / REQUESTS)
    // =======================================================

    @GET("api/requests")
    suspend fun getRequests(): Response<BaseResponse<List<Request>>>

    @POST("api/requests")
    suspend fun createRequest(@Body body: Map<String, String>): Response<BaseResponse<Request>>

    @POST("api/requests/{id}/upvote")
    suspend fun upvoteRequest(@Path("id") id: String): Response<BaseResponse<Request>>

    @POST("api/requests/{id}/resolve")
    suspend fun resolveRequest(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<BaseResponse<Request>>

    // =======================================================
    // CÁC API CHO DIỄN ĐÀN THẢO LUẬN
    // =======================================================

    // Lấy chi tiết một bài viết kèm theo danh sách bình luận
    @GET("api/requests/{id}")
    suspend fun getRequestById(@Path("id") id: String): Response<BaseResponse<Request>>

    // Thêm một bình luận mới vào bài viết
    @POST("api/requests/{id}/comment")
    suspend fun addComment(
        @Path("id") id: String,
        @Body body: Map<String, String> // Truyền vào {"content": "Nội dung bình luận"}
    ): Response<BaseResponse<Request>>

    // =======================================================
    // 7. NHẮN TIN (CHAT)
    // =======================================================

    @POST("api/chat/conversations")
    suspend fun getOrCreateConversation(@Body body: Map<String, String>): Response<BaseResponse<Conversation>>

    @GET("api/chat/messages/{conversationId}")
    suspend fun getMessages(@Path("conversationId") conversationId: String): Response<BaseResponse<List<Message>>>

    @GET("api/chat/conversations/me")
    suspend fun getMyConversations(): Response<BaseResponse<List<Conversation>>>

    // =======================================================
    // 8. BÁO CÁO VI PHẠM (REPORT)
    // =======================================================

    @POST("api/reports")
    suspend fun createReport(
        @Body body: Map<String, String>
    ): Response<BaseResponse<Any>> // Dùng Any vì ta chỉ cần lấy success và message từ server
}