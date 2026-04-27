package com.example.thuctaptotnghiep.utils

object Constants {
    // Dùng 10.0.2.2 cho máy ảo Android (Emulator)
    // Dùng IP Wi-Fi (VD: 192.168.1.xx) nếu test trên điện thoại thật cắm cáp
    // Dùng Domain thật khi đã deploy (VD: https://stushare.com)
    const val BASE_URL = "http://10.0.2.2:3000" // Cập nhật IP/Domain theo môi trường của bạn
}

// Hàm mở rộng (Extension function) để tự động nối chuỗi URL cho ảnh/file
fun String?.toFullUrl(): String {
    // Cải tiến: Thêm check an toàn để tránh trả về URL lỗi nếu chuỗi ban đầu rỗng hoặc null
    if (this.isNullOrBlank()) {
        return ""
    }

    // Nếu chuỗi đã là URL hoàn chỉnh thì giữ nguyên
    if (this.startsWith("http://") || this.startsWith("https://")) {
        return this
    }

    // Đảm bảo không bị dư hoặc thiếu dấu "/" khi nối chuỗi
    val cleanPath = if (this.startsWith("/")) this.substring(1) else this
    return "${Constants.BASE_URL}/$cleanPath"
}