package com.example.thuctaptotnghiep.ui.profile

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(), // <-- TIÊM VIEWMODEL
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToMyDocs: () -> Unit
) {
    val context = LocalContext.current

    // Quan sát các State từ ViewModel
    val myDocuments by viewModel.myDocuments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val deleteStatus by viewModel.deleteStatus.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Tài liệu đã đăng", "Đã lưu", "Đã tải về")
    var documentToDelete by remember { mutableStateOf<Document?>(null) }

    // Xử lý thông báo
    LaunchedEffect(errorMessage, deleteStatus) {
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        deleteStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetDeleteStatus()
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                onHomeClick = { onBackClick() },
                onUploadClick = { },
                onProfileClick = { },
                onSearchClick = { }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Header lượn sóng (Giữ nguyên UI đẹp)
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        lineTo(0f, size.height - 60f)
                        quadraticBezierTo(size.width * 0.4f, size.height + 20f, size.width, size.height - 100f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    drawPath(path, Color(0xFF6FB1F0))
                }

                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp).background(Color(0xFF4C9EEB), CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Row(
                    modifier = Modifier.align(Alignment.TopStart).padding(top = 50.dp, start = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(80.dp).background(Color(0xFFD9D9D9), CircleShape).clip(CircleShape))
                    Spacer(modifier = Modifier.width(16.dp))

                    // ĐÃ CẬP NHẬT: Lấy tên thật từ ViewModel thay vì gõ cứng
                    Text(text = "Xin chào, ${viewModel.userName}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-10).dp).background(Color(0xFFE0E0E0), RoundedCornerShape(20.dp)).padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xem Bảng xếp hạng", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color.Black,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = Color(0xFF4C9EEB), height = 3.dp)
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title, fontSize = 14.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Danh sách tài liệu thật từ ViewModel
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTabIndex == 0) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF4C9EEB), modifier = Modifier.align(Alignment.Center))
                    } else if (myDocuments.isEmpty()) {
                        Text("Bạn chưa đăng tài liệu nào.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(myDocuments) { doc ->
                                ProfileDocumentItem(document = doc, onDeleteClick = { documentToDelete = doc })
                            }
                        }
                    }
                } else {
                    Text("Tính năng đang phát triển...", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        // Dialog xác nhận xóa
        if (documentToDelete != null) {
            AlertDialog(
                onDismissRequest = { documentToDelete = null },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc muốn xóa '${documentToDelete!!.title}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteDocument(documentToDelete!!.id)
                        documentToDelete = null
                    }) { Text("Xóa", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { documentToDelete = null }) { Text("Hủy") }
                }
            )
        }
    }
}

@Composable
fun ProfileDocumentItem(document: Document, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(50.dp).background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text(text = document.title.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF4C9EEB), fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = document.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${document.size} • ${document.uploadDate.take(10)}", fontSize = 12.sp, color = Color.DarkGray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${document.downloads}", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}