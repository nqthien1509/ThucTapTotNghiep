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

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }

    // =======================================================
    // 2. HÀM ĐĂNG KÝ KÊNH THÔNG BÁO THEO TÊN NGƯỜI DÙNG
    // =======================================================
    private fun subscribeToFirebaseTopic() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val rawName = currentUser?.displayName

        if (!rawName.isNullOrBlank()) {
            // Xóa khoảng trắng để tạo tên kênh khớp với Backend (VD: "Thien" -> "user_Thien")
            val topicName = "user_${rawName.replace("\\s+".toRegex(), "")}"

            FirebaseMessaging.getInstance().subscribeToTopic(topicName)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM", "✅ Đã đăng ký thành công kênh nhận thông báo: $topicName")
                    } else {
                        Log.e("FCM", "❌ Đăng ký kênh thất bại", task.exception)
                    }
                }
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
                // Nếu người dùng đã cấp quyền trước đó rồi thì không làm gì cả
                Log.d("FCM", "Quyền thông báo đã được cấp sẵn.")
            } else {
                // Mở bảng pop-up xin quyền
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}