package com.example.thuctaptotnghiep.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // LƯU Ý QUAN TRỌNG:
    // Nếu bạn chạy app trên Máy ảo Android (Emulator), hãy dùng "http://10.0.2.2:3000/"
    // vì 10.0.2.2 là địa chỉ IP giúp máy ảo kết nối được với localhost của máy tính.
    // Nếu bạn chạy trên điện thoại cắm cáp thật, bạn phải đổi thành IP WiFi của máy tính (VD: http://192.168.1.x:3000/)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}