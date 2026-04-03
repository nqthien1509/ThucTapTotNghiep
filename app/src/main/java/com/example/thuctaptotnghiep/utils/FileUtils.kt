package com.example.thuctaptotnghiep.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    // Hàm này nhận vào một Uri và trả về một File thực tế
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            // 1. Xin quyền đọc nội dung từ điện thoại
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            // 2. Tạo một file tạm trong thư mục Cache của ứng dụng
            // Đặt tên tạm là upload_doc, đuôi .pdf
            val tempFile = File.createTempFile("upload_doc_", ".pdf", context.cacheDir)

            // 3. Tiến hành copy dữ liệu từ điện thoại sang file tạm
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)

            // 4. Đóng luồng để tránh rò rỉ bộ nhớ
            inputStream?.close()
            outputStream.close()

            tempFile // Trả về file đã copy thành công
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}