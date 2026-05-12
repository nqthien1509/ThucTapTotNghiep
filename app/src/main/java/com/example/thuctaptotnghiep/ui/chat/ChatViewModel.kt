package com.example.thuctaptotnghiep.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Message
import com.example.thuctaptotnghiep.data.repository.ChatRepository
import com.example.thuctaptotnghiep.utils.SocketHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentConversationId: String = ""

    // 1. Tải lịch sử tin nhắn từ API
    fun loadMessages(conversationId: String) {
        currentConversationId = conversationId
        viewModelScope.launch {
            try {
                val response = repository.getMessages(conversationId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _messages.value = response.body()?.data ?: emptyList()
                    Log.d("ChatViewModel", "Đã tải ${messages.value.size} tin nhắn cũ từ API")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Lỗi tải tin nhắn: ${e.message}")
            }
        }
    }

    // 2. Thiết lập và lắng nghe Socket
    fun setupSocket(conversationId: String) {
        currentConversationId = conversationId // Lưu ID phòng để tái sử dụng

        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        val socket = SocketHandler.getSocket()

        // Gửi sự kiện xin tham gia phòng chat
        socket?.emit("join_room", conversationId)
        Log.d("ChatViewModel", "Mở màn hình: Đã yêu cầu join_room -> $conversationId")

        // Lắng nghe tin nhắn mới từ Server gửi về
        socket?.on("receive_message") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0]
                    // Xử lý an toàn: Dữ liệu trả về có thể là JSONObject hoặc String
                    val jsonObject = if (data is String) JSONObject(data) else data as JSONObject

                    val newMessage = Message(
                        _id = jsonObject.optString("_id"),
                        conversationId = jsonObject.optString("conversationId"),
                        senderId = jsonObject.optString("senderId"),
                        text = jsonObject.optString("text"),
                        createdAt = jsonObject.optString("createdAt")
                    )

                    Log.d("ChatViewModel", "Socket nhận được tin nhắn: ${newMessage.text}")

                    // Cập nhật StateFlow (Thêm tin nhắn mới vào cuối danh sách)
                    _messages.update { currentList ->
                        // Kiểm tra trùng lặp để tránh nảy 2 tin nhắn giống nhau
                        val isDuplicate = currentList.any { it._id == newMessage._id && newMessage._id.isNotEmpty() }
                        if (!isDuplicate) {
                            currentList + newMessage
                        } else {
                            currentList
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Lỗi parse JSON khi nhận tin nhắn: ${e.message}")
                }
            }
        }
    }

    // 3. Gửi tin nhắn
    fun sendMessage(senderId: String, text: String) {
        var socket = SocketHandler.getSocket()

        // [CẬP NHẬT QUAN TRỌNG]: Kiểm tra và nối lại mạng
        if (socket == null || !socket.connected()) {
            Log.d("ChatViewModel", "Phát hiện mất kết nối Socket, đang nối lại...")
            SocketHandler.setSocket()
            SocketHandler.establishConnection()
            socket = SocketHandler.getSocket()

            // [ĐIỂM MẤU CHỐT]: Phải báo cho Server biết mình muốn chui lại vào phòng nào
            socket?.emit("join_room", currentConversationId)
            Log.d("ChatViewModel", "Đã kết nối lại và join_room -> $currentConversationId")
        }

        // Đóng gói dữ liệu thành JSON để gửi qua Socket
        val jsonObject = JSONObject().apply {
            put("conversationId", currentConversationId)
            put("senderId", senderId)
            put("text", text)
        }

        Log.d("ChatViewModel", "Đang gửi tin nhắn đi: $text")
        socket?.emit("send_message", jsonObject.toString())
    }

    // Hủy lắng nghe socket khi thoát màn hình chat
    override fun onCleared() {
        super.onCleared()
        val socket = SocketHandler.getSocket()
        socket?.off("receive_message")
        Log.d("ChatViewModel", "Thoát màn hình: Đã tắt lắng nghe receive_message")
    }
}