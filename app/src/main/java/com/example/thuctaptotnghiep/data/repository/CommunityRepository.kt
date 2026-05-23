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

    // Gọi API Upvote hoặc Bỏ upvote một yêu cầu
    suspend fun upvoteRequest(id: String): Response<BaseResponse<Request>> {
        return apiService.upvoteRequest(id)
    }

    // Gọi API đóng yêu cầu bằng cách gửi link tài liệu
    suspend fun resolveRequest(id: String, resolvedLink: String): Response<BaseResponse<Request>> {
        val body = mapOf("resolvedLink" to resolvedLink)
        return apiService.resolveRequest(id, body)
    }

    // ==========================================
    // [THÊM MỚI] - CÁC HÀM CHO DIỄN ĐÀN THẢO LUẬN
    // ==========================================

    // Gọi API lấy chi tiết bài viết và danh sách bình luận
    suspend fun getRequestById(id: String): Response<BaseResponse<Request>> {
        return apiService.getRequestById(id)
    }

    // Gọi API gửi bình luận mới
    suspend fun addComment(id: String, content: String): Response<BaseResponse<Request>> {
        val body = mapOf("content" to content)
        return apiService.addComment(id, body)
    }
}