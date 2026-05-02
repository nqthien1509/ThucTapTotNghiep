package com.example.thuctaptotnghiep.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// NÚT NỔI UPLOAD
// ==========================================
@Composable
fun UploadButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color(0xFF4C9EEB),
        contentColor = Color.White,
        modifier = modifier.size(60.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(32.dp))
    }
}

// ==========================================
// THANH ĐIỀU HƯỚNG DƯỚI ĐÁY DÙNG CHUNG CỦA APP
// ==========================================
@Composable
fun AppBottomNavigationBar(
    currentRoute: String? = null,
    onHomeClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit = {} // [CẬP NHẬT 1]: Thêm callback với giá trị mặc định là rỗng
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            containerColor = Color.White,
            contentColor = Color.Gray,
            tonalElevation = 8.dp,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                selected = currentRoute == "home",
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4C9EEB),
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color(0xFFE3F2FD)
                ),
                onClick = { if (currentRoute != "home") onHomeClick() }
            )

            NavigationBarItem(
                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                selected = currentRoute == "search",
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4C9EEB),
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color(0xFFE3F2FD)
                ),
                onClick = { if (currentRoute != "search") onSearchClick() }
            )

            Spacer(modifier = Modifier.weight(1.5f)) // Khoảng trống cho nút Upload ở giữa

            NavigationBarItem(
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                selected = currentRoute == "notifications",
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4C9EEB),
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color(0xFFE3F2FD)
                ),
                onClick = {
                    // [CẬP NHẬT 2]: Xóa dòng Toast cũ đi và gọi callback chuyển trang
                    if (currentRoute != "notifications") {
                        onNotificationClick()
                    }
                }
            )

            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                selected = currentRoute == "profile",
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4C9EEB),
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color(0xFFE3F2FD)
                ),
                onClick = { if (currentRoute != "profile") onProfileClick() }
            )
        }

        UploadButton(
            onClick = onUploadClick,
            modifier = Modifier.offset(y = (-30).dp)
        )
    }
}

// ==========================================
// TRẠNG THÁI ĐANG TẢI (LOADING)
// ==========================================
@Composable
fun LoadingStateView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Đang tải dữ liệu, vui lòng đợi" },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF4C9EEB))
    }
}

// ==========================================
// TRẠNG THÁI RỖNG (EMPTY)
// ==========================================
@Composable
fun EmptyStateView(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .semantics(mergeDescendants = true) { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// TRẠNG THÁI LỖI (ERROR)
// ==========================================
@Composable
fun ErrorStateView(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFF44336),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Đã xảy ra lỗi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = errorMessage, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB))
        ) {
            Text("Thử lại", fontWeight = FontWeight.Bold)
        }
    }
}