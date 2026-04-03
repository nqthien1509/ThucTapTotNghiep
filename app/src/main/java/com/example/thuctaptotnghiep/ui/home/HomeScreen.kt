package com.example.thuctaptotnghiep.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onNavigateToUpload: () -> Unit) {
    Scaffold(
        // Gọi BottomBar mới (đã chứa sẵn nút Upload bên trong)
        bottomBar = { AppBottomNavigationBar(onUploadClick = onNavigateToUpload) },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { HeaderSection() }
            item {
                DocumentSection(
                    title = "Mới được tải lên",
                    items = listOf("Giáo trình Mạng máy tính", "Slide An toàn bảo mật", "Đề cương Triết học")
                )
            }
            item {
                DocumentSection(
                    title = "Tài liệu ôn thi",
                    items = listOf("Đề thi Toán cao cấp 2024", "Giải bài tập Vật lý 1", "Tổng hợp trắc nghiệm")
                )
            }
            item {
                DocumentSection(
                    title = "Tin nổi bật",
                    items = listOf("Cẩm nang bảo vệ đồ án", "Kỹ năng phỏng vấn IT", "Hướng dẫn viết CV")
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// 1. Phần Header màu xanh
@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF4C9EEB),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Avatar", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Xin chào,", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text("Thien", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            placeholder = { Text("Tìm kiếm tài liệu...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(26.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// 2. Nhóm danh sách cuộn ngang
@Composable
fun DocumentSection(title: String, items: List<String>) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Xem tất cả", color = Color(0xFF4C9EEB), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items.size) { index ->
                DocumentCardPreview(title = items[index])
            }
        }
    }
}

// 3. Thẻ tài liệu hoàn chỉnh
@Composable
fun DocumentCardPreview(title: String) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Ảnh bìa giả lập
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFE3F2FD)), // Nền xanh nhạt
                contentAlignment = Alignment.Center
            ) {
                // Đã đổi MenuBook thành Star để không bị lỗi
                Icon(Icons.Default.Star, contentDescription = "Cover", tint = Color(0xFF4C9EEB), modifier = Modifier.size(40.dp))
            }
            // Thông tin tài liệu
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "PDF • Miễn phí",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// 4. Nút nổi Upload (Thêm modifier để điều chỉnh vị trí)
@Composable
fun UploadButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color(0xFF4C9EEB),
        contentColor = Color.White,
        modifier = modifier.size(60.dp), // Kích thước nút
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(32.dp))
    }
}

// 5. Thanh điều hướng dưới đáy (Gom chung NavigationBar và Nút Upload)
@Composable
fun AppBottomNavigationBar(onUploadClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter // Căn xuống đáy
    ) {
        // Thanh nền trắng
        NavigationBar(
            containerColor = Color.White,
            contentColor = Color.Gray,
            tonalElevation = 8.dp,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                selected = true,
                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF4C9EEB), unselectedIconColor = Color.Gray, indicatorColor = Color.White),
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                selected = false,
                colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray),
                onClick = {}
            )

            // Khoảng trống lớn ở giữa nhường chỗ cho nút tròn
            Spacer(modifier = Modifier.weight(1.5f))

            NavigationBarItem(
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                selected = false,
                colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray),
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                selected = false,
                colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray),
                onClick = {}
            )
        }

        // Nút tròn kéo lún xuống thanh bar
        UploadButton(
            onClick = onUploadClick,
            modifier = Modifier.offset(y = (-30).dp) // Kéo lên trên 30dp so với đáy
        )
    }
}