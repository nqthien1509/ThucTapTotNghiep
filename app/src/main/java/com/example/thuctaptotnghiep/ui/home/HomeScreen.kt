package com.example.thuctaptotnghiep.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage // <-- IMPORT THƯ VIỆN COIL
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.example.thuctaptotnghiep.utils.UserManager // <-- IMPORT TRẠM TRUNG CHUYỂN

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToUpload: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current

    val documentList by viewModel.documents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // ==========================================
    // CẬP NHẬT: Lắng nghe Dữ liệu User từ Trạm Trung Chuyển (Real-time)
    // ==========================================
    val currentUserProfile by UserManager.userProfile.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                onHomeClick = { /* Đang ở Home */ },
                onUploadClick = onNavigateToUpload,
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // Truyền đối tượng User và Tên mặc định (từ Firebase) vào Header
            item {
                HeaderSection(
                    userProfile = currentUserProfile,
                    fallbackName = viewModel.userName,
                    onSearchClick = onSearchClick
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4C9EEB))
                    }
                }
            } else {
                item { DocumentSection(title = "Mới được tải lên", items = documentList, onItemClick = onDocumentClick) }
                item { DocumentSection(title = "Tài liệu ôn thi", items = documentList.shuffled(), onItemClick = onDocumentClick) }
                item { DocumentSection(title = "Tin nổi bật", items = documentList, onItemClick = onDocumentClick) }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
fun HeaderSection(userProfile: User?, fallbackName: String, onSearchClick: () -> Unit) {
    // Ưu tiên hiển thị tên từ MongoDB (đã đổi), nếu chưa đổi thì lấy tên Firebase
    val displayUserName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: fallbackName

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF4C9EEB), shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // ==========================================
            // CẬP NHẬT: HIỂN THỊ AVATAR ĐỒNG BỘ TỪ SERVER
            // ==========================================
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (!userProfile?.avatarUrl.isNullOrBlank()) {
                    // Load ảnh từ Server Node.js (10.0.2.2)
                    val fullImageUrl = "http://10.0.2.2:3000${userProfile!!.avatarUrl}"
                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop, // Cắt cho vừa hình tròn
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Nếu chưa có ảnh, hiện chữ cái đầu của Tên
                    Text(
                        text = displayUserName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Xin chào,", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                // Hiển thị tên đã đồng bộ
                Text(displayUserName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth().height(52.dp).background(Color.White, RoundedCornerShape(26.dp)).clip(RoundedCornerShape(26.dp))
                .clickable { onSearchClick() }.padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Tìm kiếm tài liệu...", color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun DocumentSection(title: String, items: List<Document>, onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Xem tất cả", color = Color(0xFF4C9EEB), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            Text("Chưa có tài liệu nào.", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items.size) { index ->
                    DocumentCardPreview(document = items[index], onClick = { onItemClick(items[index].id) })
                }
            }
        }
    }
}

@Composable
fun DocumentCardPreview(document: Document, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(140.dp).height(200.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                Text(text = document.title.take(1).uppercase(), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4C9EEB))
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = document.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.weight(1f))
                document.size?.let { Text(text = it, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
            }
        }
    }
}