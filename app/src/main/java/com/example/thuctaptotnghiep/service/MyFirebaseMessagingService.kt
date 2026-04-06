package com.example.thuctaptotnghiep.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.thuctaptotnghiep.R // Import file R của bạn
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Hàm này tự động chạy khi có thông báo từ Worker gửi xuống
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Lấy Tiêu đề và Nội dung từ thông báo
        val title = remoteMessage.notification?.title ?: "Thông báo mới"
        val body = remoteMessage.notification?.body ?: "Bạn có một cập nhật mới."

        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "kiem_duyet_tai_lieu"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo Channel (Bắt buộc cho Android 8.0 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo Kiểm duyệt",
                NotificationManager.IMPORTANCE_HIGH // Mức độ ưu tiên cao để hiện pop-up
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Thiết kế khung thông báo
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icon mặc định, bạn có thể đổi icon app của bạn
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Hiển thị lên màn hình
        notificationManager.notify(Random.nextInt(), notification)
    }

    // Hàm này cấp lại Token mới nếu Google thay đổi mã thiết bị
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Log.d("FCM", "Mã thiết bị mới: $token")
    }
}