package com.example.thuctaptotnghiep.data.model

import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("_id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("authorName")
    val authorName: String,

    @SerializedName("fileUrl")
    val fileUrl: String,

    @SerializedName("size")
    val size: String?, // Có thể để nullable (?) phòng trường hợp file chưa kịp tính dung lượng

    @SerializedName("downloads")
    val downloads: Int = 0,

    @SerializedName("views")
    val views: Int = 0,

    @SerializedName("uploadDate")
    val uploadDate: String,

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("isFavorite")
    val isFavorite: Boolean = false,

    @SerializedName("isWatchLater")
    val isWatchLater: Boolean = false
)