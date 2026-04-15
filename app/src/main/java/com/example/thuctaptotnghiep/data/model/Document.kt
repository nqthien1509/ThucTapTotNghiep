package com.example.thuctaptotnghiep.data.model

import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("_id") val id: String,

    // CẬP NHẬT TRỌNG TÂM: Thêm userId để đồng bộ với Backend mới
    @SerializedName("userId") val userId: String? = null,

    @SerializedName("title") val title: String,
    @SerializedName("authorName") val authorName: String,

    @SerializedName("subject") val subject: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("tags") val tags: List<String> = emptyList(),

    @SerializedName("fileUrl") val fileUrl: String,
    @SerializedName("size") val size: String? = null,
    @SerializedName("uploadDate") val uploadDate: String,

    @SerializedName("status") val status: String = "pending",
    @SerializedName("downloads") val downloads: Int = 0,
    @SerializedName("views") val views: Int = 0,

    // Các trạng thái tương tác trả về từ Backend
    @SerializedName("isFavorite") val isFavorite: Boolean = false,
    @SerializedName("isWatchLater") val isWatchLater: Boolean = false
)