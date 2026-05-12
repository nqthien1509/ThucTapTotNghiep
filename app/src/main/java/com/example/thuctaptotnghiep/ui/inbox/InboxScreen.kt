package com.example.thuctaptotnghiep.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.utils.toFullUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    navController: NavController,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val myId = viewModel.currentUserId

    // Tự động load lại mỗi khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.fetchConversations()
    }

    Scaffold(
        topBar = {
            // [CẬP NHẬT UI]: Header Gradient bo góc sâu
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
                        Text("Tin nhắn", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, // Đục thủng để thấy Gradient
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        containerColor = Color(0xFFF8F9FA) // Nền xám nhạt làm nổi bật Card trắng
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (isLoading && conversations.isEmpty()) {
                CircularProgressIndicator(
                    color = Color(0xFF4C9EEB),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (conversations.isEmpty()) {
                // Trạng thái Rỗng xịn xò
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Hộp thư trống", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Chưa có cuộc trò chuyện nào diễn ra.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Cấu trúc danh sách lơ lửng
                ) {
                    items(conversations) { conversation ->
                        val otherUser = conversation.participants?.firstOrNull { it.id != myId }

                        InboxItem(
                            otherUser = otherUser,
                            lastMessage = conversation.lastMessage ?: "Bắt đầu trò chuyện...",
                            onClick = {
                                navController.navigate("chat/${conversation.id}")
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun InboxItem(otherUser: User?, lastMessage: String, onClick: () -> Unit) {
    val displayName = otherUser?.displayName ?: "Người dùng ẩn danh"

    // [CẬP NHẬT UI]: Card Item thay cho Divider thô cứng
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hiển thị Avatar thông minh
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFE3F2FD), CircleShape) // Xanh nhạt dịu mắt
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!otherUser?.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = otherUser?.avatarUrl?.toFullUrl(),
                        contentDescription = "Avatar của $displayName",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = displayName.take(1).uppercase(),
                        color = Color(0xFF4C9EEB),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    maxLines = 1, // Cắt dòng nếu tin nhắn quá dài
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}