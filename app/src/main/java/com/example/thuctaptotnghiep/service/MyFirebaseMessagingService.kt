package com.example.thuctaptotnghiep.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.thuctaptotnghiep.MainActivity
import com.example.thuctaptotnghiep.R
import com.example.thuctaptotnghiep.data.model.AppNotification
import com.example.thuctaptotnghiep.utils.NotificationEventBus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Lấy dữ liệu từ push
        val title = remoteMessage.notification?.title ?: "Thông báo mới"
        val body = remoteMessage.notification?.body ?: ""
        val dataPayload = remoteMessage.data

        // 1. Gửi thông báo lên hệ thống (Thanh trạng thái của điện thoại)
        sendNotification(title, body, dataPayload)

        // 2. Chuyển đổi data thành Model AppNotification để update giao diện Real-time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentTimeIso = dateFormat.format(Date())

        val newNotification = AppNotification(
            _id = "temp_${System.currentTimeMillis()}", // Tạo ID tạm thời, khi refresh sẽ lấy ID thật từ DB
            title = title,
            body = body,
            isRead = false,
            data = dataPayload,
            createdAt = currentTimeIso
        )

        // 3. Bắn event đi để ViewModel cập nhật giao diện ngay lập tức
        CoroutineScope(Dispatchers.Main).launch {
            NotificationEventBus.emitNewNotification(newNotification)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Lưu token lên server nếu bạn cần gửi đích danh cho thiết bị này (hiện tại bạn đang dùng topic nên có thể bỏ qua)
    }

    private fun sendNotification(title: String, messageBody: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Truyền thêm data vào Intent để xử lý khi người dùng nhấn từ Status Bar
        data.forEach { (key, value) ->
            intent.putExtra(key, value)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "thuctaptotnghiep_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Thay đổi icon cho phù hợp
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Yêu cầu từ Android 8.0 (Oreo) trở lên phải có Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Kênh thông báo tài liệu",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}