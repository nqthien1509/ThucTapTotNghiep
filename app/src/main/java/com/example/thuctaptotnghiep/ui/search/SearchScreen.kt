package com.example.thuctaptotnghiep.ui.search

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar // Import Bottom Bar dùng chung

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onHomeClick: () -> Unit,      // THÊM: Điều hướng về Home
    onUploadClick: () -> Unit,    // THÊM: Điều hướng đến Upload
    onProfileClick: () -> Unit    // THÊM: Điều hướng đến Profile
) {
    var searchQuery by remember { mutableStateOf("") }

    // Dữ liệu giả lập
    val allDocuments = listOf(
        "Giáo trình Mạng máy tính", "Slide An toàn bảo mật", "Đề cương Triết học",
        "Đề thi Toán cao cấp 2024", "Giải bài tập Vật lý 1", "Tổng hợp trắc nghiệm",
        "Cẩm nang bảo vệ đồ án", "Kỹ năng phỏng vấn IT", "Hướng dẫn viết CV"
    )

    val recentSearches = remember { mutableStateListOf("Toán rời rạc", "Cấu trúc dữ liệu", "Lập trình Android") }
    val suggestions = listOf("Tiếng Anh TOEIC", "Bảo mật mạng", "Quản trị dự án", "Đồ án tốt nghiệp")

    val searchResults = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        allDocuments.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color.White,
        // GẮN THANH ĐIỀU HƯỚNG DƯỚI ĐÁY VÀO ĐÂY
        bottomBar = {
            AppBottomNavigationBar(
                onHomeClick = onHomeClick,
                onUploadClick = onUploadClick,
                onProfileClick = onProfileClick,
                onSearchClick = { /* Đang ở trang tìm kiếm rồi nên không cần làm gì */ }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // 1. Vẽ nền lượn sóng màu xanh
            Canvas(modifier = Modifier.fillMaxWidth().height(230.dp).align(Alignment.TopCenter)) {
                val path = Path().apply {
                    lineTo(0f, size.height - 60f)
                    quadraticBezierTo(size.width * 0.5f, size.height, size.width, size.height - 60f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, Color(0xFF6FB1F0))
            }

            // Nút Back
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 16.dp, start = 8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.Black)
            }

            // 2. Nội dung chính
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "Tìm Kiếm",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Ô nhập liệu
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(52.dp),
                    placeholder = { Text("Tìm kiếm tài liệu...", color = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(26.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 3. Khung xám kết quả
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFEEEEEE), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(20.dp)
                ) {
                    if (searchQuery.isBlank()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (recentSearches.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    HorizontalDivider(modifier = Modifier.width(16.dp), color = Color.Gray)
                                    Text("Tìm kiếm gần đây", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray)
                                    Text("Xóa", fontWeight = FontWeight.Bold, color = Color.DarkGray, modifier = Modifier.padding(start = 8.dp).clickable { recentSearches.clear() })
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    recentSearches.forEach { keyword ->
                                        SearchKeywordChip(text = keyword, onClick = { searchQuery = keyword })
                                    }
                                }
                                Spacer(modifier = Modifier.height(40.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                HorizontalDivider(modifier = Modifier.width(16.dp), color = Color.Gray)
                                Text("Gợi ý cho bạn", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                suggestions.forEach { keyword ->
                                    SearchKeywordChip(text = keyword, onClick = { searchQuery = keyword })
                                }
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(searchResults.size) { index ->
                                SearchResultItem(
                                    title = searchResults[index],
                                    onClick = { onDocumentClick(searchResults[index]) }
                                )
                            }

                            if (searchResults.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                                        Text("Không tìm thấy tài liệu nào!", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchKeywordChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFF6FB1F0), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SearchResultItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF4C9EEB))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Định dạng: PDF", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}