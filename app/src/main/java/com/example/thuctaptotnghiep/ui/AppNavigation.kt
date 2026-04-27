package com.example.thuctaptotnghiep.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination // Bổ sung import để lấy node gốc
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.thuctaptotnghiep.ui.auth.LoginScreen
import com.example.thuctaptotnghiep.ui.auth.RegisterScreen
import com.example.thuctaptotnghiep.ui.detail.DocumentDetailScreen
import com.example.thuctaptotnghiep.ui.home.HomeScreen
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
    // HÀM TỐI ƯU ĐIỀU HƯỚNG BOTTOM BAR (CHUẨN GOOGLE)
    // =======================================================
    val navigateToBottomTab = { route: String ->
        navController.navigate(route) {
            // CẢI TIẾN: Thay vì hard-code chuỗi "home", dùng findStartDestination().id
            // Giúp dọn dẹp Back Stack an toàn tuyệt đối bất kể màn hình khởi đầu là gì.
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true // Lưu lại trạng thái của tab bị đè
            }
            launchSingleTop = true // Không mở thêm màn hình mới nếu đã đang ở đúng tab đó
            restoreState = true    // Phục hồi lại trạng thái cũ khi quay lại (VD: Vị trí cuộn trang)
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
                onSearchClick = { navigateToBottomTab("search") },
                // Bắt sự kiện Xem tất cả và truyền category sang màn Search
                onNavigateToSeeAll = { category ->
                    navController.navigate("search?category=$category")
                }
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
                onSearchClick = { navigateToBottomTab("search") },
                onNavigateToDetail = { id -> navController.navigate("document_detail/$id") }
            )
        }

        // 6. Màn hình Hồ sơ (Profile)
        composable("profile") {
            ProfileScreen(
                onBackClick = { navigateToBottomTab("home") },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        // Lệnh mạnh nhất để xóa sạch 100% Back Stack khi đăng xuất
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
        composable(
            route = "search?category={category}",
            arguments = listOf(navArgument("category") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")

            SearchScreen(
                initialCategory = category,
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { id -> navController.navigate("document_detail/$id") },
                onHomeClick = { navigateToBottomTab("home") },
                onUploadClick = { navigateToBottomTab("upload") },
                onProfileClick = { navigateToBottomTab("profile") }
            )
        }
    }
}