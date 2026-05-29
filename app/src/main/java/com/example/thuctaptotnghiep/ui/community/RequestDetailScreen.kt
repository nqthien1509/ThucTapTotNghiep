package com.example.thuctaptotnghiep.ui.community

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.thuctaptotnghiep.data.model.Comment
import com.example.thuctaptotnghiep.data.model.Request
import com.example.thuctaptotnghiep.utils.toFullUrl
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    requestId: String,
    onNavigateBack: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val request by viewModel.selectedRequest.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current

    var commentText by remember { mutableStateOf("") }

    // ==========================================
    // STATE CHO TÍNH NĂNG BÁO CÁO (REPORT)
    // ==========================================
    var commentToReport by remember { mutableStateOf<Comment?>(null) }
    var reportReason by remember { mutableStateOf("") }

    // Gọi API lấy chi tiết bài viết ngay khi vào màn hình
    LaunchedEffect(requestId) {
        viewModel.fetchRequestDetail(requestId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thảo luận", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1E293B),
                    navigationIconContentColor = Color(0xFF1E293B)
                )
            )
        },
        bottomBar = {
            // Thanh nhập bình luận ở dưới cùng
            CommentInputBar(
                text = commentText,
                onTextChange = { commentText = it },
                onSend = {
                    if (commentText.isNotBlank()) {
                        viewModel.addComment(requestId, commentText) {
                            commentText = "" // Xóa text sau khi gửi thành công
                        }
                    }
                },
                isLoading = isLoading
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (request == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isLoading) CircularProgressIndicator(color = Color(0xFF4C9EEB))
                else Text("Không tìm thấy bài viết", color = Color.Gray)
            }
        } else {
            val req = request!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 1. Bài viết gốc
                item {
                    OriginalPostSection(
                        request = req,
                        currentUserId = currentUserId,
                        onUpvoteClick = { viewModel.upvoteRequest(req.id) }
                    )
                }

                // 2. Tiêu đề phần bình luận
                item {
                    Text(
                        text = "Bình luận (${req.comments.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                // 3. Danh sách các bình luận
                items(req.comments) { comment ->
                    CommentItem(
                        comment = comment,
                        currentUserId = currentUserId,
                        onReportClick = { commentToReport = comment }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOG NHẬP LÝ DO BÁO CÁO VI PHẠM
    // ==========================================
    if (commentToReport != null) {
        AlertDialog(
            onDismissRequest = { commentToReport = null; reportReason = "" },
            title = { Text("Báo cáo vi phạm", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Bạn đang báo cáo người dùng ${commentToReport?.user?.displayName ?: "Ẩn danh"}. Vui lòng cho biết lý do:",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Ví dụ: Sử dụng ngôn từ khiếm nhã...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetUid = commentToReport?.user?.id ?: "" // Đã khớp với Model User.kt
                        if (targetUid.isNotBlank() && reportReason.isNotBlank()) {
                            viewModel.reportUser(
                                targetId = targetUid,
                                reason = reportReason,
                                evidenceLink = requestId,
                                onSuccess = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    commentToReport = null
                                    reportReason = ""
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Vui lòng nhập lý do báo cáo!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Gửi báo cáo")
                }
            },
            dismissButton = {
                TextButton(onClick = { commentToReport = null; reportReason = "" }) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun OriginalPostSection(request: Request, currentUserId: String, onUpvoteClick: () -> Unit) {
    val authorName = request.author?.displayName ?: "Ẩn danh"
    val isUpvoted = request.upvotes.contains(currentUserId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Thông tin người đăng
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!request.author?.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = request.author?.avatarUrl?.toFullUrl(),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.LightGray)
                )
            } else {
                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFFE3F2FD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = authorName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF4C9EEB))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = authorName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "Đang tìm tài liệu", fontSize = 12.sp, color = Color.Gray)
            }

            if (request.status == "resolved") {
                Row(
                    modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Đã có tài liệu", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nội dung bài viết
        Text(text = request.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = request.description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569), lineHeight = 22.sp)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
        Spacer(modifier = Modifier.height(8.dp))

        // Nút Upvote
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onUpvoteClick() }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ThumbUp,
                contentDescription = "Upvote",
                tint = if (isUpvoted) Color(0xFF4C9EEB) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${request.upvotes.size} Đẩy lên",
                color = if (isUpvoted) Color(0xFF4C9EEB) else Color.Gray,
                fontWeight = if (isUpvoted) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String,
    onReportClick: () -> Unit
) {
    val userName = comment.user?.displayName ?: "Ẩn danh"
    val commentAuthorId = comment.user?.id ?: "" // Đã map đúng với file User.kt của bạn

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar người bình luận
        if (!comment.user?.avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = comment.user?.avatarUrl?.toFullUrl(),
                contentDescription = "Avatar",
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray)
            )
        } else {
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = userName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Bong bóng chat chứa nội dung
        Column(
            modifier = Modifier
                .weight(1f) // Cần weight để đẩy nút Báo cáo sang lề phải
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = comment.content, fontSize = 14.sp, color = Color(0xFF334155))
        }

        // ==========================================
        // ICON BÁO CÁO (Chỉ hiện khi không phải bình luận của chính mình)
        // ==========================================
        if (commentAuthorId != currentUserId && commentAuthorId.isNotBlank()) {
            IconButton(
                onClick = onReportClick,
                modifier = Modifier.size(36.dp).padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = "Báo cáo vi phạm",
                    tint = Color(0xFFCBD5E1) // Màu xám nhạt (Slate 300) để không làm rối mắt
                )
            }
        }
    }
}

@Composable
fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp)),
                placeholder = { Text("Viết bình luận...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading,
                modifier = Modifier.background(if (text.isNotBlank()) Color(0xFF4C9EEB) else Color.LightGray, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi", tint = Color.White)
            }
        }
    }
}