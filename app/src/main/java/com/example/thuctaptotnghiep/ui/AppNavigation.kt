package com.example.thuctaptotnghiep.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.thuctaptotnghiep.ui.auth.LoginScreen
import com.example.thuctaptotnghiep.ui.auth.RegisterScreen
import com.example.thuctaptotnghiep.ui.detail.DocumentDetailScreen
import com.example.thuctaptotnghiep.ui.home.HomeScreen
import com.example.thuctaptotnghiep.ui.management.MyDocumentsScreen
import com.example.thuctaptotnghiep.ui.profile.ProfileScreen
import com.example.thuctaptotnghiep.ui.search.SearchScreen
import com.example.thuctaptotnghiep.ui.upload.UploadScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Kiểm tra trạng thái đăng nhập để chọn màn hình khởi đầu
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startRoute = if (currentUser != null) "home" else "login"

    NavHost(navController = navController, startDestination = startRoute) {

        // 1. Màn hình Đăng nhập
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 2. Màn hình Đăng ký
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 3. Màn hình Chính (Home)
        composable("home") {
            HomeScreen(
                onNavigateToUpload = { navController.navigate("upload") },
                onDocumentClick = { id ->
                    navController.navigate("document_detail/$id")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onSearchClick = {
                    navController.navigate("search")
                }
            )
        }

        // 4. Màn hình Chi tiết tài liệu (Có truyền ID)
        composable(
            route = "document_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("id") ?: ""

            DocumentDetailScreen(
                documentId = documentId,
                onBackClick = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onUploadClick = { navController.navigate("upload") },
                onProfileClick = { navController.navigate("profile") },
                onSearchClick = { navController.navigate("search") }
            )
        }

        // 5. Màn hình Upload tài liệu
        composable("upload") {
            UploadScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onUploadClick = { /* Đang ở chính nó */ },
                onProfileClick = { navController.navigate("profile") },
                onSearchClick = { navController.navigate("search") }
            )
        }

        // 6. Màn hình Hồ sơ (Đã cập nhật các tham số mới)
        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    // Đăng xuất và xóa sạch lịch sử để về màn login
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                onUploadClick = {
                    navController.navigate("upload")
                }
            )
        }

        // 7. Màn hình Tìm kiếm
        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { id ->
                    navController.navigate("document_detail/$id")
                },
                onHomeClick = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onUploadClick = { navController.navigate("upload") },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        // 8. Màn hình phụ: Quản lý tài liệu của tôi (Nếu cần dùng riêng)
        composable("my_documents") {
            MyDocumentsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}