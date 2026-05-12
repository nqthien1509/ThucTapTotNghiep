package com.example.thuctaptotnghiep.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thuctaptotnghiep.data.model.AppNotification
import com.example.thuctaptotnghiep.utils.FileUtils.getFriendlyTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onNavigateToDocument: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchNotifications(isRefresh = true)
    }

    Scaffold(
        topBar = {
            // [CẬP NHẬT UI]: Header Gradient bo góc sâu đồng bộ với toàn app
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), spotColor = Color(0x33000000))
                    .background(
                        brush = Brush.verticalGradient(listOf(Color(0xFF4C9EEB), Color(0xFF1E88E5))),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                TopAppBar(
                    title = {
                        Text("Thông báo", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.markAllAsRead() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            // Bọc icon bằng vòng tròn nền mờ
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = "Đánh dấu đã đọc tất cả",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, // Đục thủng để hiện màu Gradient
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        containerColor = Color(0xFFF8F9FA) // Màu nền tổng thể sáng và sang trọng
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && notifications.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF4C9EEB))
            } else if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chưa có thông báo nào", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(
                        items = notifications,
                        key = { it._id }
                    ) { notification ->

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteNotification(notification._id)
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(16.dp)), // Bo góc cho mảng vuốt màu đỏ
                            backgroundContent = {
                                val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                    Color(0xFFFF4D4D) else Color.Transparent // Đỏ tươi hơn
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.White)
                                    }
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification._id)
                                    }
                                    val docId = notification.data?.get("documentId")
                                    if (docId != null) {
                                        onNavigateToDocument(docId)
                                    }
                                }
                            )
                        }
                    }

                    item {
                        if (!viewModel.isLastPage) {
                            LaunchedEffect(Unit) {
                                viewModel.fetchNotifications(isRefresh = false)
                            }
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = Color(0xFF4C9EEB))
                            }
                        } else if (notifications.isNotEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Bạn đã xem hết thông báo", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit
) {
    // Nếu chưa đọc thì nền trắng (bóng nổi), đã đọc thì nền xám xanh nhạt chìm xuống
    val backgroundColor = if (notification.isRead) Color(0xFFF1F5F9) else Color.White

    val notificationType = notification.data?.get("type")

    // Tạo Palette màu dựa trên loại thông báo
    val (icon, baseColor) = when (notificationType) {
        "LIKE" -> Pair(Icons.Rounded.Favorite, Color(0xFFE91E63)) // Hồng đậm
        "UPLOAD" -> Pair(Icons.Rounded.UploadFile, Color(0xFF4C9EEB)) // Xanh app
        else -> Pair(Icons.Rounded.Notifications, Color(0xFFF59E0B)) // Vàng cam
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (notification.isRead) 0.dp else 4.dp, // Chưa đọc thì đổ bóng cao hơn
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon lồng trong hình tròn nền nhạt
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(baseColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = baseColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.ExtraBold,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body,
                    fontSize = 14.sp,
                    color = if (notification.isRead) Color(0xFF64748B) else Color(0xFF334155),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = getFriendlyTime(notification.createdAt),
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            // Chấm xanh (Blue dot) biểu thị chưa đọc
            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF4C9EEB), CircleShape)
                )
            }
        }
    }
}