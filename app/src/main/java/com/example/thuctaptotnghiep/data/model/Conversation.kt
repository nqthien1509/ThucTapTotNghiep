package com.example.thuctaptotnghiep.data.model

import com.google.gson.annotations.SerializedName

data class Conversation(
    @SerializedName("_id") val id: String,
    val requestId: String?,
    // Đổi sang List<User> để chứa thông tin người nhắn cùng
    val participants: List<User>?,
    val lastMessage: String?,
    val updatedAt: String?
)