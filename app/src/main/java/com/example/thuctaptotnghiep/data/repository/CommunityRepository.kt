package com.example.thuctaptotnghiep.data.repository

import com.example.thuctaptotnghiep.data.model.BaseResponse
import com.example.thuctaptotnghiep.data.model.Request
import com.example.thuctaptotnghiep.data.network.ApiService
import retrofit2.Response
import javax.inject.Inject

class CommunityRepository @Inject constructor(
    private val apiService: ApiService
) {
    // Gọi API lấy danh sách yêu cầu tài liệu
    suspend fun getRequests(): Response<BaseResponse<List<Request>>> {
        return apiService.getRequests()
    }

    // Gọi API tạo yêu cầu mới
    suspend fun createRequest(title: String, description: String): Response<BaseResponse<Request>> {
        val body = mapOf(
            "title" to title,
            "description" to description
        )
        return apiService.createRequest(body)
    }
}