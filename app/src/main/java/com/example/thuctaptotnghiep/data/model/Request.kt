package com.example.thuctaptotnghiep.data.model

import com.google.gson.annotations.SerializedName

data class Request(
    @SerializedName("_id") val id: String, // Chuẩn hóa từ _id sang id
    @SerializedName("author") val author: User?,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("status") val status: String, // "open", "resolved", "closed"
    @SerializedName("createdAt") val createdAt: String,

    // ============================================================
    // [THÊM MỚI] - CÁC TRƯỜNG PHỤC VỤ CHO LUỒNG DIỄN ĐÀN THẢO LUẬN
    // ============================================================
    @SerializedName("upvotes") val upvotes: List<String> = emptyList(), // Danh sách UID đã upvote
    @SerializedName("resolvedLink") val resolvedLink: String? = null,
    @SerializedName("resolvedBy") val resolvedBy: User? = null,
    @SerializedName("comments") val comments: List<Comment> = emptyList() // Danh sách các bình luận
)

// Cấu trúc dữ liệu của một Bình luận trong bài viết công đồng
data class Comment(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: User?, // Thông tin người bình luận (Đã được BE tự động populate)
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String
)