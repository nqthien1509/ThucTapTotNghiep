package com.example.thuctaptotnghiep.ui.home

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToUpload: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit // THÊM: Nhận sự kiện bấm Tìm kiếm
) {
    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                onUploadClick = onNavigateToUpload,
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick // Truyền xuống Bottom Bar
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { HeaderSection(onSearchClick = onSearchClick) } // Truyền vào Header
            item {
                DocumentSection(
                    title = "Mới được tải lên",
                    items = listOf("Giáo trình Mạng máy tính", "Slide An toàn bảo mật", "Đề cương Triết học"),
                    onItemClick = onDocumentClick
                )
            }
            item {
                DocumentSection(
                    title = "Tài liệu ôn thi",
                    items = listOf("Đề thi Toán cao cấp 2024", "Giải bài tập Vật lý 1", "Tổng hợp trắc nghiệm"),
                    onItemClick = onDocumentClick
                )
            }
            item {
                DocumentSection(
                    title = "Tin nổi bật",
                    items = listOf("Cẩm nang bảo vệ đồ án", "Kỹ năng phỏng vấn IT", "Hướng dẫn viết CV"),
                    onItemClick = onDocumentClick
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// 1. Phần Header màu xanh
@Composable
fun HeaderSection(onSearchClick: () -> Unit) { // Nhận sự kiện click
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

        // Box giả lập thanh TextField để bấm vào sẽ chuyển trang thay vì bật bàn phím
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .clickable { onSearchClick() } // Bấm vào đây sẽ gọi lệnh chuyển trang
                .padding(horizontal = 16.dp),
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

// 2. Nhóm danh sách cuộn ngang
@Composable
fun DocumentSection(title: String, items: List<String>, onItemClick: (String) -> Unit) {
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
                DocumentCardPreview(
                    title = items[index],
                    onClick = { onItemClick(items[index]) }
                )
            }
        }
    }
}

// 3. Thẻ tài liệu hoàn chỉnh
@Composable
fun DocumentCardPreview(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = "Cover", tint = Color(0xFF4C9EEB), modifier = Modifier.size(40.dp))
            }
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

// 4. Nút nổi Upload
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

// 5. Thanh điều hướng dưới đáy
@Composable
fun AppBottomNavigationBar(
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit // THÊM: Tham số click Tìm kiếm
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
                selected = true,
                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF4C9EEB), unselectedIconColor = Color.Gray, indicatorColor = Color.White),
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                selected = false,
                colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray),
                onClick = onSearchClick // Gắn lệnh vào icon Kính lúp
            )

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
                onClick = onProfileClick
            )
        }

        UploadButton(
            onClick = onUploadClick,
            modifier = Modifier.offset(y = (-30).dp)
        )
    }
}