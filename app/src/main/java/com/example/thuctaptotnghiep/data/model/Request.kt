package com.example.thuctaptotnghiep.data.model

data class Request(
    val _id: String,
    val author: User?, // Tham chiếu đến model User hiện tại của bạn
    val title: String,
    val description: String,
    val status: String,
    val createdAt: String
)