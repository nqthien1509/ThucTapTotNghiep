package com.example.thuctaptotnghiep.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    navController: NavController,
    conversationId: String,
    currentUserId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // Gọi 1 lần khi mở màn hình: Load tin nhắn cũ & Kết nối Socket
    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
        viewModel.setupSocket(conversationId)
    }

    Scaffold(
        topBar = {
            // [CẬP NHẬT UI]: Thanh Header trắng tinh khôi, đổ bóng nhẹ
            Surface(
                shadowElevation = 4.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Text("Trò chuyện", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E293B))
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        containerColor = Color(0xFFF8F9FA), // Nền xám nhạt giúp nổi bật bong bóng chat
        bottomBar = {
            // [CẬP NHẬT UI]: Thanh nhập tin nhắn hiện đại, có bóng đổ ngược lên
            Surface(
                shadowElevation = 16.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Nhập tin nhắn...", color = Color(0xFF94A3B8), fontSize = 15.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9), // Nền xám xanh cực nhạt
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFF4C9EEB)
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        maxLines = 4 // Hỗ trợ gõ tin nhắn dài tự xuống dòng
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Nút Send tròn, màu Gradient hoặc solid blue
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(currentUserId, inputText.trim())
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (inputText.isNotBlank()) Color(0xFF4C9EEB) else Color(0xFFE2E8F0),
                                shape = CircleShape
                            ),
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Gửi",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp).offset(x = 2.dp) // Dịch nhẹ icon send cho cân tâm
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Khung hiển thị tin nhắn
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            reverseLayout = true // Đảo ngược để cuộn từ dưới lên
        ) {
            items(messages.reversed()) { msg ->
                val isMe = msg.senderId == currentUserId
                MessageBubble(text = msg.text, isMe = isMe)
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, isMe: Boolean) {
    // Khoảng cách giữa các tin nhắn
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = if (isMe) 2.dp else 4.dp, // Đổ bóng nhẹ cho tin nhắn người kia
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isMe) 20.dp else 4.dp, // Khuyết góc đuôi bong bóng
                        bottomEnd = if (isMe) 4.dp else 20.dp
                    ),
                    spotColor = Color(0x1A000000)
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isMe) 20.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 20.dp
                    )
                )
                .background(
                    if (isMe) Brush.horizontalGradient(listOf(Color(0xFF4C9EEB), Color(0xFF3B82F6))) // Gradient cho tin nhắn của mình
                    else Brush.horizontalGradient(listOf(Color.White, Color.White)) // Trắng cho tin nhắn người kia
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(min = 40.dp, max = 280.dp) // Giới hạn chiều rộng tin nhắn không bị tràn
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = if (isMe) Color.White else Color(0xFF1E293B) // Trắng cho mình, Xám đen cho đối phương
            )
        }
    }
}