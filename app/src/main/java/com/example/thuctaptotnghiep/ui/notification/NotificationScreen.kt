package com.example.thuctaptotnghiep.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // [THÊM]: Import icon nút Back
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.thuctaptotnghiep.data.model.AppNotification
import com.example.thuctaptotnghiep.utils.FileUtils.getFriendlyTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onNavigateToDocument: (String) -> Unit, // Callback để mở document
    onBackClick: () -> Unit // [THÊM]: Callback xử lý sự kiện bấm nút Quay lại
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Mỗi khi màn hình NotificationScreen được khởi tạo, gọi API lấy dữ liệu mới nhất
    LaunchedEffect(Unit) {
        viewModel.fetchNotifications(isRefresh = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo") },
                // [THÊM]: Nút mũi tên Quay lại ở bên trái
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Đánh dấu đã đọc tất cả")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && notifications.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (notifications.isEmpty()) {
                Text("Chưa có thông báo nào", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                            backgroundContent = {
                                val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                    Color.Red else Color.Transparent
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
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
                                    // 1. Đánh dấu đã đọc trên Server và Local
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification._id)
                                    }

                                    // 2. Mở Document nếu có
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else if (notifications.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Bạn đã xem hết thông báo", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
    val backgroundColor = if (notification.isRead) MaterialTheme.colorScheme.surface else Color(0xFFE3F2FD)

    val notificationType = notification.data?.get("type")
    val (icon, iconTint) = when (notificationType) {
        "LIKE" -> Pair(Icons.Default.Favorite, Color.Red)
        "UPLOAD" -> Pair(Icons.Default.UploadFile, MaterialTheme.colorScheme.primary)
        else -> Pair(Icons.Default.Notifications, MaterialTheme.colorScheme.primary)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = getFriendlyTime(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
    }
}