package com.example.thuctaptotnghiep.di

import com.example.thuctaptotnghiep.data.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Module này sẽ sống trong toàn bộ vòng đời của App
object NetworkModule {

    @Provides
    @Singleton // Chỉ tạo ra 1 bản sao Retrofit duy nhất để tiết kiệm bộ nhớ
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            // ĐÂY LÀ NƠI ĐIỀN LINK SERVER GỐC CỦA BẠN
            .baseUrl("https://api.vidu-thuctap.com/")
            .addConverterFactory(GsonConverterFactory.create()) // Dùng Gson để đọc hiểu JSON
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}