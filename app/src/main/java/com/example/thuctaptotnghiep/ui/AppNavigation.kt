package com.example.thuctaptotnghiep.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.example.thuctaptotnghiep.ui.notification.NotificationScreen
import com.example.thuctaptotnghiep.ui.notification.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(initialDocumentId: String? = null) {
    val navController = rememberNavController()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val startRoute = if (currentUser != null) "home" else "login"

    LaunchedEffect(initialDocumentId) {
        if (!initialDocumentId.isNullOrEmpty() && currentUser != null) {
            navController.navigate("document_detail/$initialDocumentId")
        }
    }

    val navigateToBottomTab = { route: String ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = navController, startDestination = startRoute) {

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

        composable("home") {
            HomeScreen(
                onNavigateToUpload = { navigateToBottomTab("upload") },
                onDocumentClick = { id -> navController.navigate("document_detail/$id") },
                onProfileClick = { navigateToBottomTab("profile") },
                onSearchClick = { navigateToBottomTab("search") },
                onNavigateToSeeAll = { category ->
                    navController.navigate("search?category=$category")
                },
                onNotificationClick = {
                    navController.navigate("notifications")
                }
            )
        }

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

        composable("profile") {
            ProfileScreen(
                onBackClick = { navigateToBottomTab("home") },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
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

        composable("notifications") {
            val viewModel: NotificationViewModel = hiltViewModel()

            NotificationScreen(
                viewModel = viewModel,
                onNavigateToDocument = { documentId ->
                    navController.navigate("document_detail/$documentId")
                },
                // [THÊM]: Xử lý sự kiện quay lại bằng cách popBackStack
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}