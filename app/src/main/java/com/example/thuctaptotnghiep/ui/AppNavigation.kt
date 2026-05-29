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
import com.example.thuctaptotnghiep.ui.community.RequestDetailScreen
import com.example.thuctaptotnghiep.ui.chat.ChatRoomScreen
import com.example.thuctaptotnghiep.ui.chat.InboxScreen
import com.example.thuctaptotnghiep.ui.onboarding.OnboardingScreen
import com.example.thuctaptotnghiep.ui.splash.SplashScreen
import com.example.thuctaptotnghiep.ui.leaderboard.LeaderboardScreen

import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(initialDocumentId: String? = null) {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    val sharedPreferences = remember {
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

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
            action()
        } else {
            navController.navigate("login") {
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

        composable("splash") {
            SplashScreen(
                onTimeout = {
                    val isOnboardingCompleted = sharedPreferences.getBoolean("is_onboarding_completed", false)
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
                    sharedPreferences.edit().putBoolean("is_onboarding_completed", true).apply()
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // =======================================================
        // [CẢI TIẾN] LUỒNG ĐĂNG NHẬP / ĐĂNG KÝ
        // =======================================================
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Kiểm tra xem phía sau Login có màn hình nào không
                    val previousRoute = navController.previousBackStackEntry?.destination?.route

                    if (previousRoute != null && previousRoute != "splash") {
                        // Nếu là Khách (Guest) đang lướt app rồi bấm vào tính năng yêu cầu đăng nhập -> Lùi lại đúng màn hình đó
                        navController.popBackStack()
                    } else {
                        // Nếu vừa Đăng xuất xong hoặc mở app -> Xóa trắng lịch sử và đẩy thẳng vào Home
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register") { launchSingleTop = true }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // Đăng ký thành công -> Xóa trắng ngăn xếp và vào Home
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
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
                onLeaderboardClick = { navController.navigate("leaderboard") { launchSingleTop = true } },
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

        // =======================================================
        // [CẢI TIẾN] LUỒNG ĐĂNG XUẤT (LOGOUT)
        // =======================================================
        composable("profile") {
            ProfileScreen(
                onBackClick = { navigateToBottomTab("home") },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    // Khi Đăng xuất: Về trang Login và dùng popUpTo(0) để CẮT ĐỨT mọi lịch sử phía sau
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
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

        composable(
            route = "request_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("id") ?: ""

            RequestDetailScreen(
                requestId = requestId,
                onNavigateBack = { navController.popBackStack() }
            )
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

        composable("leaderboard") {
            LeaderboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDocumentDetail = { id ->
                    navController.navigate("document_detail/$id") { launchSingleTop = true }
                }
            )
        }
    }
}