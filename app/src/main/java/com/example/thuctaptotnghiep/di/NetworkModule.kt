package com.example.thuctaptotnghiep.di

import com.example.thuctaptotnghiep.data.network.ApiService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 1. Tạo Interceptor Tích hợp: Gắn Trace ID + Firebase Auth Token
    @Provides
    @Singleton
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            // --- A. GẮN TRACE ID (Cho hệ thống Logging) ---
            val requestId = UUID.randomUUID().toString()
            requestBuilder.addHeader("x-request-id", requestId)

            // --- B. GẮN FIREBASE TOKEN (Để Backend xác thực) ---
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                try {
                    // Lấy token đồng bộ (bắt buộc dùng Tasks.await khi ở trong Interceptor background)
                    val task = user.getIdToken(false)
                    val tokenResult = Tasks.await(task, 10, TimeUnit.SECONDS) // Chờ tối đa 10s
                    val token = tokenResult?.token

                    if (token != null) {
                        // Thêm chữ Bearer theo đúng chuẩn chuẩn JWT
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                } catch (e: Exception) {
                    e.printStackTrace() // Log ra nếu rớt mạng hoặc lỗi token
                }
            }

            // Tiếp tục thực hiện Request với các Header đã được gắn
            chain.proceed(requestBuilder.build())
        }
    }

    // 2. Nhét Interceptor vào OkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS) // Tăng thời gian chờ tránh timeout
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // 3. Tạo Retrofit
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val BASE_URL = "http://10.0.2.2:3000/"

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 4. Cung cấp ApiService
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}