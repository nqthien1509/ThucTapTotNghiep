package com.example.thuctaptotnghiep.data.model

// Lớp Generic <T> giúp bạn bọc bất kỳ kiểu dữ liệu nào (List<Request>, Conversation, Message...)
data class BaseResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String? = null
)