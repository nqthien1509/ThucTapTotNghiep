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

    // [CẬP NHẬT TRỌNG TÂM]: Xử lý triệt để lỗi dư dấu "/" khi nối chuỗi
    // Lấy base URL từ cấu hình
    val baseUrl = BuildConfig.BASE_URL

    // Xóa dấu "/" ở cuối Base URL (nếu có)
    val cleanBaseUrl = baseUrl.removeSuffix("/")

    // Xóa dấu "/" ở đầu Path (nếu có)
    val cleanPath = this.removePrefix("/")

    // Nối lại đảm bảo chỉ có đúng 1 dấu "/" ở giữa
    return "$cleanBaseUrl/$cleanPath"
}