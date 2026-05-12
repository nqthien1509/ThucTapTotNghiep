package com.example.thuctaptotnghiep.data.model

data class Message(
    val _id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val createdAt: String
)