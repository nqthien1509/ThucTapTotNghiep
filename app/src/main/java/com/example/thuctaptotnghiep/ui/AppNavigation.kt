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

    // Kiểm tra xem đã có ai đăng nhập trên máy này chưa
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startRoute = if (currentUser != null) "home" else "login"

    // NavHost là nơi chứa các màn hình
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
                onDocumentClick = { id -> // Đã sửa thành id
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

        // 4. Màn hình Chi tiết tài liệu
        composable(
            route = "document_detail/{id}", // Đã sửa thành id
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("id") ?: "" // Lấy id từ URL

            DocumentDetailScreen(
                documentId = documentId, // Truyền đúng tên biến documentId
                onBackClick = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onUploadClick = { navController.navigate("upload") },
                onProfileClick = { navController.navigate("profile") },
                onSearchClick = { navController.navigate("search") }
            )
        }

        // 5. Màn hình Đăng tài liệu (Upload)
        composable("upload") {
            UploadScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onUploadClick = { /* Không làm gì vì đang ở Upload */ },
                onProfileClick = { navController.navigate("profile") },
                onSearchClick = { navController.navigate("search") }
            )
        }

        // 6. Màn hình Hồ sơ (Profile)
        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToMyDocs = {
                    navController.navigate("my_documents")
                }
            )
        }

        // 7. Màn hình Tìm kiếm (Search)
        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { id -> // Đã sửa thành id
                    navController.navigate("document_detail/$id")
                },
                onHomeClick = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onUploadClick = { navController.navigate("upload") },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        // 8. Màn hình Quản lý tài liệu của tôi
        composable("my_documents") {
            MyDocumentsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}