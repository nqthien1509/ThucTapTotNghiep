package com.example.thuctaptotnghiep.data.model

data class AppNotification(
    val _id: String,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val data: Map<String, String>?,
    val createdAt: String
)