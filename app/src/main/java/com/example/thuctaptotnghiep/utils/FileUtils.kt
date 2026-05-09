package com.example.thuctaptotnghiep.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object FileUtils {

    // =======================================================
    // 1. Cải tiến uriToFile: Lấy đúng tên file gốc và an toàn bộ nhớ
    // =======================================================
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver

            // Lấy tên file gốc (ví dụ: tailieu.docx, baocao.pdf) thay vì hardcode ".pdf"
            var fileName = "upload_doc_${System.currentTimeMillis()}"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }

            // Tạo file tạm với tên gốc trong thư mục Cache
            val tempFile = File(context.cacheDir, fileName)

            // Dùng hàm 'use' của Kotlin để TỰ ĐỘNG ĐÓNG LUỒNG (InputStream/OutputStream)
            // Đảm bảo không bao giờ bị rò rỉ bộ nhớ
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            tempFile // Trả về file đã copy thành công
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // =======================================================
    // 2. Thêm getFriendlyTime để xử lý hiển thị thời gian cho Thông báo
    // =======================================================
    fun getFriendlyTime(dateString: String): String {
        try {
            // Định dạng thời gian trả về từ MongoDB (chuẩn ISO 8601)
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC") // MongoDB lưu giờ UTC

            val pastDate = format.parse(dateString) ?: return dateString
            val now = Date()

            val diffMillis = now.time - pastDate.time
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
            val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

            return when {
                seconds < 60 -> "Vừa xong"
                minutes < 60 -> "$minutes phút trước"
                hours < 24 -> "$hours giờ trước"
                days == 1L -> "Hôm qua"
                days < 7 -> "$days ngày trước"
                else -> {
                    // Nếu quá 7 ngày thì hiển thị dạng ngày tháng năm (VD: 09/05/2026)
                    val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    displayFormat.format(pastDate)
                }
            }
        } catch (e: Exception) {
            return dateString // Nếu lỗi parse thì trả về chuỗi gốc
        }
    }
}