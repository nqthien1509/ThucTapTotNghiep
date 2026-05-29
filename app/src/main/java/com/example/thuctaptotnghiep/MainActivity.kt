package com.example.thuctaptotnghiep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.thuctaptotnghiep.ui.AppNavigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // =======================================================
    // 1. TRÌNH XIN QUYỀN THÔNG BÁO (Dành cho Android 13+)
    // =======================================================
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "✅ Người dùng đã cấp quyền thông báo!")
        } else {
            Log.d("FCM", "❌ Người dùng từ chối quyền thông báo!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gọi hàm xin quyền hiển thị thông báo
        askNotificationPermission()

        // Gọi hàm đăng ký trạm thu sóng Firebase
        subscribeToFirebaseTopic()

        // =======================================================
        // LẤY VÀ IN FIREBASE ID TOKEN ĐỂ DÙNG CHO WEB ADMIN (Nếu cần thiết)
        // =======================================================
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token
                    Log.e("MY_ADMIN_TOKEN", "============ BẮT ĐẦU TOKEN ============")
                    Log.e("MY_ADMIN_TOKEN", token ?: "Token rỗng!")
                    Log.e("MY_ADMIN_TOKEN", "============ KẾT THÚC TOKEN ============")
                } else {
                    Log.e("MY_ADMIN_TOKEN", "Lấy token thất bại", task.exception)
                }
            }
        }

        // Lấy documentId từ Push Notification (nếu người dùng nhấn từ Status Bar)
        val documentIdFromPush = intent.getStringExtra("documentId")

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Truyền documentId vào AppNavigation
                    AppNavigation(initialDocumentId = documentIdFromPush)
                }
            }
        }
    }

    // =======================================================
    // 2. HÀM ĐĂNG KÝ KÊNH THÔNG BÁO (CÁ NHÂN & TOÀN HỆ THỐNG)
    // =======================================================
    private fun subscribeToFirebaseTopic() {
        val messaging = FirebaseMessaging.getInstance()

        // [MỚI THÊM]: Đăng ký kênh toàn hệ thống để nhận Broadcast từ Admin
        messaging.subscribeToTopic("all_users")
            .addOnSuccessListener { Log.d("FCM", "✅ Đã đăng ký kênh Broadcast: all_users") }
            .addOnFailureListener { Log.e("FCM", "❌ Đăng ký kênh Broadcast thất bại", it) }

        // Kênh thông báo cá nhân (Giữ nguyên)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid

        if (!uid.isNullOrBlank()) {
            val sanitizedUid = uid.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_")
            val topicName = "user_$sanitizedUid"

            messaging.subscribeToTopic(topicName)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM", "✅ Đã đăng ký thành công kênh cá nhân: $topicName")
                    } else {
                        Log.e("FCM", "❌ Đăng ký kênh cá nhân thất bại", task.exception)
                    }
                }
        } else {
            Log.w("FCM", "⚠️ Chưa đăng nhập, chỉ nhận được thông báo chung (all_users)")
        }
    }

    // =======================================================
    // 3. HÀM KIỂM TRA VÀ XIN QUYỀN (CHỈ CHẠY TRÊN ANDROID 13+)
    // =======================================================
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("FCM", "Quyền thông báo đã được cấp sẵn.")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}