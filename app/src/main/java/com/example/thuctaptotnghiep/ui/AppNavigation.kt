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

    // =======================================================
    // HÀM TỐI ƯU ĐIỀU HƯỚNG BOTTOM BAR (CHỐNG TRÀN BỘ NHỚ)
    // =======================================================
    val navigateToBottomTab = { route: String ->
        navController.navigate(route) {
            // Quay về "home" làm gốc để không tạo ra chuỗi màn hình dài dằng dặc
            popUpTo("home") {
                saveState = true // Lưu lại trạng thái của tab (VD: người dùng đang cuộn tới đâu)
            }
            launchSingleTop = true // Không mở thêm màn hình mới nếu đã đang ở đúng tab đó
            restoreState = true    // Phục hồi lại trạng thái cũ khi quay lại
        }
    }

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
                onNavigateToUpload = { navigateToBottomTab("upload") },
                onDocumentClick = { id -> navController.navigate("document_detail/$id") },
                onProfileClick = { navigateToBottomTab("profile") },
                onSearchClick = { navigateToBottomTab("search") }
            )
        }

        // 4. Màn hình Chi tiết tài liệu
        composable(
            route = "document_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("id") ?: ""

            DocumentDetailScreen(
                documentId = documentId,
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navigateToBottomTab("home") },
                onUploadClick = { navigateToBottomTab("upload") },
                onProfileClick = { navigateToBottomTab("profile") },
                onSearchClick = { navigateToBottomTab("search") }
            )
        }

        // 5. Màn hình Upload tài liệu
        composable("upload") {
            UploadScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navigateToBottomTab("home") },
                onUploadClick = { /* Đang ở chính nó */ },
                onProfileClick = { navigateToBottomTab("profile") },
                onSearchClick = { navigateToBottomTab("search") }
            )
        }

        // 6. Màn hình Hồ sơ (Profile)
        composable("profile") {
            ProfileScreen(
                // Do ProfileScreen gắn onBackClick vào nút Home ở thanh dưới, ta truyền hàm về trang chủ
                onBackClick = { navigateToBottomTab("home") },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        // popUpTo(navController.graph.id) là lệnh mạnh nhất để xóa sạch 100% Back Stack
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onSearchClick = { navigateToBottomTab("search") },
                onUploadClick = { navigateToBottomTab("upload") },
                onDocumentClick = { documentId ->
                    navController.navigate("document_detail/$documentId")
                }
            )
        }

        // 7. Màn hình Tìm kiếm (Search)
        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { id -> navController.navigate("document_detail/$id") },
                onHomeClick = { navigateToBottomTab("home") },
                onUploadClick = { navigateToBottomTab("upload") },
                onProfileClick = { navigateToBottomTab("profile") }
            )
        }

        // 8. Màn hình phụ: Quản lý tài liệu
        composable("my_documents") {
            MyDocumentsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}