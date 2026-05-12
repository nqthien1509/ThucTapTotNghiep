package com.example.thuctaptotnghiep.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.thuctaptotnghiep.R // Đảm bảo import đúng R của dự án bạn
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit // Hàm gọi khi hết thời gian
) {
    // Hiệu ứng mờ dần (Fade-in) cho Logo
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Chạy hiệu ứng fade-in trong 1 giây
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        // Giữ lại thêm 1 giây nữa (Tổng cộng 2 giây)
        delay(1000)
        // Hết thời gian, gọi hàm chuyển trang
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(), // Tránh lẹm vào status bar (pin, wifi)
        contentAlignment = Alignment.Center
    ) {
        // Vùng chứa Logo và Tên app
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha.value) // Áp dụng hiệu ứng mờ dần
        ) {
            // Hiển thị Logo
            Image(
                painter = painterResource(id = R.drawable.logo_stushare), // Đặt tên file logo trong drawable là logo_stushare
                contentDescription = "Logo StuShare",
                modifier = Modifier
                    .size(150.dp) // Kích thước Logo
            )
        }
    }
}