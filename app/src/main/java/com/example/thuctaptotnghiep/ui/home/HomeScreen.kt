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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar

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
            // ĐÃ CẬP NHẬT: Truyền đúng tên từ Firebase vào Header
            item { HeaderSection(userName = viewModel.userName, onSearchClick = onSearchClick) }

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
fun HeaderSection(userName: String, onSearchClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF4C9EEB), shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = "Avatar", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Xin chào,", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                // ĐÃ SỬA: Hiển thị biến userName thay vì chữ "Thien"
                Text(userName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
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
                // Hiển thị chữ cái đầu tiên của sách làm ảnh bìa tạm
                Text(text = document.title.take(1).uppercase(), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4C9EEB))
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = document.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = document.size, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}