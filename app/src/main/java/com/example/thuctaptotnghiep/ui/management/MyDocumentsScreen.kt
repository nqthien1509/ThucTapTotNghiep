package com.example.thuctaptotnghiep.ui.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDocumentsScreen(
    onBackClick: () -> Unit
) {
    // Dữ liệu giả lập các tài liệu mà user ĐÃ UPLOAD
    val myDocs = remember {
        mutableStateListOf(
            "Giáo trình Mạng máy tính",
            "Đề cương Triết học Mác Lênin",
            "Báo cáo thực tập Mẫu 2023"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài liệu của tôi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (myDocs.isEmpty()) {
                // Hiển thị khi không có tài liệu nào
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Bạn chưa tải lên tài liệu nào.", color = Color.Gray)
                    }
                }
            } else {
                // Hiển thị danh sách tài liệu
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp)
                ) {
                    items(myDocs.size) { index ->
                        MyDocumentItem(
                            title = myDocs[index],
                            date = "04/04/2026", // Giả lập ngày đăng
                            onEditClick = { /* TODO: Chuyển sang màn hình Edit */ },
                            onDeleteClick = {
                                // Xóa giả lập trên UI
                                myDocs.removeAt(index)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Widget giao diện cho 1 tài liệu của user
@Composable
fun MyDocumentItem(
    title: String,
    date: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon đại diện sách
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF4C9EEB))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tên sách và Ngày đăng
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Đã đăng: $date", color = Color.Gray, fontSize = 12.sp)
            }

            // Nút Sửa
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color.Gray)
            }
            // Nút Xóa
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFFF5252)) // Màu đỏ
            }
        }
    }
}