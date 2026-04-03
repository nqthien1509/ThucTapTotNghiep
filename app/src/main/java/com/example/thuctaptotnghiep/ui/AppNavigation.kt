package com.example.thuctaptotnghiep.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thuctaptotnghiep.ui.auth.LoginScreen
import com.example.thuctaptotnghiep.ui.auth.RegisterScreen // Import màn hình Đăng ký
import com.example.thuctaptotnghiep.ui.home.HomeScreen
import com.example.thuctaptotnghiep.ui.upload.UploadScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Kiểm tra xem đã có ai đăng nhập trên máy này chưa
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startRoute = if (currentUser != null) "home" else "login"

    // NavHost là nơi chứa các màn hình, startDestination là màn hình xuất hiện đầu tiên
    NavHost(navController = navController, startDestination = startRoute) {

        // 1. Màn hình Đăng nhập
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Chuyển sang Home và XÓA màn hình Login khỏi lịch sử (để không back lại được)
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 2. Màn hình Đăng ký đã được kết nối hoàn chỉnh
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // Firebase tự động đăng nhập sau khi tạo tài khoản thành công
                    // Nên ta chuyển thẳng vào Home và xóa sạch lịch sử trang Auth
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Người dùng bấm "Đã có tài khoản", chỉ cần lùi lại 1 bước là về Login
                    navController.popBackStack()
                }
            )
        }

        // 3. Màn hình Chính (Home)
        composable("home") {
            HomeScreen(
                onNavigateToUpload = { navController.navigate("upload") }
            )
        }

        // 4. Màn hình Đăng tài liệu (Upload)
        composable("upload") {
            UploadScreen(
                onBackClick = { navController.popBackStack() } // Lùi lại trang trước
            )
        }
    }
}