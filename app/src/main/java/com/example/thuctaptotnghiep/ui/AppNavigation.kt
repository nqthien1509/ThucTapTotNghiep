package com.example.thuctaptotnghiep.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import com.example.thuctaptotnghiep.ui.community.CommunityScreen
import com.example.thuctaptotnghiep.ui.chat.ChatRoomScreen
import com.example.thuctaptotnghiep.ui.chat.InboxScreen
import com.example.thuctaptotnghiep.ui.onboarding.OnboardingScreen
// [MỚI]: Import màn hình Splash (đảm bảo đúng đường dẫn package của bạn nhé)
import com.example.thuctaptotnghiep.ui.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(initialDocumentId: String? = null) {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    // Khởi tạo SharedPreferences để lưu trạng thái Onboarding
    val sharedPreferences = remember {
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    // =======================================================
    // [CẬP NHẬT TRỌNG TÂM]: THAY ĐỔI MÀN HÌNH KHỞI ĐỘNG MẶC ĐỊNH LÀ SPLASH
    // =======================================================
    // Màn hình khởi động luôn là Splash
    val startRoute = "splash"

    LaunchedEffect(initialDocumentId) {
        if (!initialDocumentId.isNullOrEmpty() && currentUser != null) {
            navController.navigate("document_detail/$initialDocumentId") {
                launchSingleTop = true
            }
        }
    }

    // =======================================================
    // HÀM BẢO VỆ (GUEST MODE) - KIỂM TRA ĐĂNG NHẬP
    // =======================================================
    val requireAuth = { action: () -> Unit ->
        if (FirebaseAuth.getInstance().currentUser != null) {
            action() // Đã đăng nhập -> Cho phép thao tác
        } else {
            navController.navigate("login") { // Chưa đăng nhập -> Đi tới Login
                launchSingleTop = true
            }
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

        // =======================================================
        // [MỚI]: ROUTE CHO MÀN HÌNH SPLASH (MÀN HÌNH CHÍNH)
        // =======================================================
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    // 1. Kiểm tra trạng thái Onboarding
                    val isOnboardingCompleted = sharedPreferences.getBoolean("is_onboarding_completed", false)

                    // 2. Chuyển hướng dựa trên trạng thái và xóa màn Splash khỏi backstack
                    if (isOnboardingCompleted) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onFinishOnboarding = {
                    // Lưu cờ đánh dấu đã hoàn thành Onboarding vĩnh viễn
                    sharedPreferences.edit().putBoolean("is_onboarding_completed", true).apply()

                    // Chuyển sang Home và xóa Onboarding khỏi ngăn xếp (Không back lại được)
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate("register") { launchSingleTop = true }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToUpload = { requireAuth { navigateToBottomTab("upload") } },
                onProfileClick = { requireAuth { navigateToBottomTab("profile") } },
                onNotificationClick = { requireAuth { navController.navigate("notifications") { launchSingleTop = true } } },
                onCommunityClick = { requireAuth { navController.navigate("community") { launchSingleTop = true } } },

                onSearchClick = { navigateToBottomTab("search") },
                onDocumentClick = { id -> navController.navigate("document_detail/$id") { launchSingleTop = true } },
                onNavigateToSeeAll = { category -> navController.navigate("search?category=$category") { launchSingleTop = true } }
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
                onSearchClick = { navigateToBottomTab("search") },
                onUploadClick = { requireAuth { navigateToBottomTab("upload") } },
                onProfileClick = { requireAuth { navigateToBottomTab("profile") } },
                onRequireLogin = { navController.navigate("login") { launchSingleTop = true } }
            )
        }

        composable("upload") {
            UploadScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navigateToBottomTab("home") },
                onUploadClick = { },
                onProfileClick = { requireAuth { navigateToBottomTab("profile") } },
                onSearchClick = { navigateToBottomTab("search") },
                onNavigateToDetail = { id ->
                    navController.navigate("document_detail/$id") {
                        popUpTo("upload") { inclusive = true }
                        launchSingleTop = true
                    }
                }
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
                onUploadClick = { requireAuth { navigateToBottomTab("upload") } },
                onDocumentClick = { documentId ->
                    navController.navigate("document_detail/$documentId") { launchSingleTop = true }
                }
            )
        }

        composable(
            route = "search?category={category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")

            SearchScreen(
                initialCategory = category,
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { id -> navController.navigate("document_detail/$id") { launchSingleTop = true } },
                onHomeClick = { navigateToBottomTab("home") },
                onUploadClick = { requireAuth { navigateToBottomTab("upload") } },
                onProfileClick = { requireAuth { navigateToBottomTab("profile") } },
                onNotificationClick = { requireAuth { navController.navigate("notifications") { launchSingleTop = true } } }
            )
        }

        composable("notifications") {
            val viewModel: NotificationViewModel = hiltViewModel()
            NotificationScreen(
                viewModel = viewModel,
                onNavigateToDocument = { documentId -> navController.navigate("document_detail/$documentId") { launchSingleTop = true } },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("community") {
            CommunityScreen(navController = navController)
        }

        composable("inbox") {
            InboxScreen(navController = navController)
        }

        composable(
            route = "chat/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            ChatRoomScreen(navController = navController, conversationId = conversationId, currentUserId = currentUserId)
        }
    }
}