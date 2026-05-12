package com.example.thuctaptotnghiep.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.thuctaptotnghiep.data.model.Request
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val requests by viewModel.requests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // [CẬP NHẬT UI]: TopAppBar đồng bộ Gradient với các màn hình khác
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, spotColor = Color(0x33000000))
                    .background(Brush.verticalGradient(listOf(Color(0xFF4C9EEB), Color(0xFF1E88E5))))
            ) {
                TopAppBar(
                    title = { Text("Cộng đồng học tập", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        // Nút Hộp thư có nền mờ nhẹ để nổi bật
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .clip(CircleShape)
                                .clickable { navController.navigate("inbox") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Hộp thư", modifier = Modifier.size(20.dp))
                        }
                    }
                )
            }
        },
        containerColor = Color(0xFFF8F9FA), // Nền xám sáng tôn các Card màu trắng
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF4C9EEB),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo yêu cầu")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            when {
                isLoading && requests.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF4C9EEB))
                }
                error != null -> {
                    Text(text = error ?: "", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                requests.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.Forum, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chưa có bài đăng nào", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Hãy là người đầu tiên tạo yêu cầu tài liệu nhé!", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Khoảng cách rộng rãi hơn
                    ) {
                        items(requests) { request ->
                            RequestItem(
                                request = request,
                                onReplyClick = {
                                    val receiverId = request.author?.id ?: ""
                                    if (receiverId.isNotBlank()) {
                                        viewModel.getOrCreateChat(request._id, receiverId) { conversationId ->
                                            navController.navigate("chat/$conversationId")
                                        }
                                    }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) } // Chừa không gian cho nút FAB
                    }
                }
            }

            if (showCreateDialog) {
                CreateRequestDialog(
                    onDismiss = { showCreateDialog = false },
                    onSubmit = { title, description ->
                        viewModel.createRequest(title, description) {
                            showCreateDialog = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RequestItem(request: Request, onReplyClick: () -> Unit) {
    val authorName = request.author?.displayName ?: "Ẩn danh"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Hàng đầu: Avatar + Tên người đăng
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFE3F2FD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = authorName.take(1).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4C9EEB),
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = authorName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Text(text = "Đang tìm kiếm tài liệu", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tiêu đề + Nội dung
            Text(
                text = request.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Nút bấm "Trả lời" hiện đại
            Button(
                onClick = onReplyClick,
                modifier = Modifier.align(Alignment.End).height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF1F5F9), // Nền xám xanh nhạt
                    contentColor = Color(0xFF4C9EEB)    // Chữ màu xanh app
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Rounded.Reply, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hỗ trợ bạn này", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CreateRequestDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp), // Bo góc mượt mà
        title = {
            Text("Đăng yêu cầu", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E293B))
        },
        text = {
            Column {
                Text("Mọi người sẽ thấy và giúp bạn tìm tài liệu này.", fontSize = 14.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(16.dp))

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF4C9EEB),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên tài liệu cần xin", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả chi tiết môn học...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB))
            ) {
                Text("Đăng bài", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    )
}