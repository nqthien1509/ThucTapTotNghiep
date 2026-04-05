package com.example.thuctaptotnghiep.data.model // Giữ nguyên package cũ của bạn

import com.google.gson.annotations.SerializedName

// Cấu trúc mới khớp 100% với MongoDB Backend
data class Document(
    @SerializedName("_id") val id: String,
    val title: String,
    val authorName: String,
    val fileUrl: String,
    val size: String,
    val downloads: Int,
    val views: Int,
    val uploadDate: String
)