package com.example.thuctaptotnghiep.utils

import com.example.thuctaptotnghiep.BuildConfig

object Constants {
    // Đã xóa BASE_URL hardcode. Mọi thứ giờ được quản lý tập trung ở build.gradle.kts
}

// Hàm mở rộng (Extension function) để tự động nối chuỗi URL cho ảnh/file
fun String?.toFullUrl(): String {
    if (this.isNullOrBlank()) {
        return ""
    }

    // Nếu chuỗi đã là URL hoàn chỉnh thì giữ nguyên
    if (this.startsWith("http://") || this.startsWith("https://")) {
        return this
    }

    // [CẬP NHẬT]: Đọc base URL từ BuildConfig (Tự động đổi theo Debug/Release)
    val baseUrl = BuildConfig.BASE_URL
    val cleanPath = if (this.startsWith("/")) this.substring(1) else this

    return "$baseUrl/$cleanPath"
}