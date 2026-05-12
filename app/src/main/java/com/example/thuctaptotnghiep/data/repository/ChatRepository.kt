package com.example.thuctaptotnghiep.data.repository

import com.example.thuctaptotnghiep.data.model.BaseResponse
import com.example.thuctaptotnghiep.data.model.Conversation
import com.example.thuctaptotnghiep.data.model.Message
import com.example.thuctaptotnghiep.data.network.ApiService
import retrofit2.Response
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    // 1. Gọi API để lấy ID phòng chat hoặc tạo mới (khi bấm "Trả lời")
    suspend fun getOrCreateConversation(requestId: String, receiverId: String): Response<BaseResponse<Conversation>> {
        val body = mapOf(
            "requestId" to requestId,
            "receiverId" to receiverId
        )
        return apiService.getOrCreateConversation(body)
    }

    // 2. Lấy lịch sử tin nhắn của một phòng chat
    suspend fun getMessages(conversationId: String): Response<BaseResponse<List<Message>>> {
        return apiService.getMessages(conversationId)
    }

    // ========================================================
    // 3. [MỚI THÊM]: Lấy danh sách hộp thư Inbox của User hiện tại
    // ========================================================
    suspend fun getMyConversations(): Response<BaseResponse<List<Conversation>>> {
        return apiService.getMyConversations()
    }
}