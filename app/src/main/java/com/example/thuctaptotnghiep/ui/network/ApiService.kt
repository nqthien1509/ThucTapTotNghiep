package com.example.thuctaptotnghiep.network

import com.example.thuctaptotnghiep.data.model.Document
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE // THÊM IMPORT NÀY
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
        @Part("authorName") author: RequestBody
    ): Any

    @GET("api/documents")
    suspend fun getDocuments(): List<Document>

    @GET("api/documents/{id}")
    suspend fun getDocumentById(@Path("id") id: String): Document

    @GET("api/search")
    suspend fun searchDocuments(@Query("q") keyword: String): List<Document>

    // --- THÊM 2 API MỚI VÀO ĐÂY ---

    // Lấy tài liệu của "Thien"
    @GET("api/my-documents/{authorName}")
    suspend fun getMyDocuments(@Path("authorName") authorName: String): List<Document>

    // Xóa tài liệu
    @DELETE("api/documents/{id}")
    suspend fun deleteDocument(@Path("id") id: String): Any
}