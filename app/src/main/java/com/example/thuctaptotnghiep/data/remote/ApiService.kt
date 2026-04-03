package com.example.thuctaptotnghiep.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    // @Multipart bắt buộc phải có khi bạn muốn upload File (như PDF, Hình ảnh)
    @Multipart
    @POST("api/documents/upload") // Đường dẫn phụ của API (Sửa lại sau cho khớp với Backend)
    suspend fun uploadDocument(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Any> // Tạm thời để Any, sau này sẽ tạo Data Class trả về cụ thể

}