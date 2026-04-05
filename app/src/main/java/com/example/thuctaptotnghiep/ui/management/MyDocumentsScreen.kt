package com.example.thuctaptotnghiep.ui.management

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
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
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun MyDocumentsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var myDocuments by remember { mutableStateOf<List<Document>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Biến quản lý hộp thoại xác nhận xóa
    var documentToDelete by remember { mutableStateOf<Document?>(null) }

    // Gọi API lấy sách do "Thien" đăng
    fun loadMyDocuments() {
        isLoading = true
        coroutineScope.launch {
            try {
                // Tạm thời truyền cứng tên "Thien", sau này có Login thật sẽ lấy từ Firebase
                myDocuments = RetrofitClient.apiService.getMyDocuments("Thien")
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Tự động chạy lần đầu mở màn hình
    LaunchedEffect(Unit) {
        loadMyDocuments()
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5)).clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Tài liệu của tôi", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // Danh sách
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF4C9EEB), modifier = Modifier.align(Alignment.Center))
                } else if (myDocuments.isEmpty()) {
                    Text("Bạn chưa tải lên tài liệu nào.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myDocuments) { doc ->
                            MyDocumentItem(
                                document = doc,
                                onDeleteClick = { documentToDelete = doc } // Mở hộp thoại xác nhận
                            )
                        }
                    }
                }
            }
        }

        // Hộp thoại xác nhận xóa
        if (documentToDelete != null) {
            AlertDialog(
                onDismissRequest = { documentToDelete = null },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa tài liệu '${documentToDelete!!.title}' không? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = documentToDelete!!.id
                            documentToDelete = null

                            // Gọi API Xóa
                            coroutineScope.launch {
                                try {
                                    RetrofitClient.apiService.deleteDocument(id)
                                    Toast.makeText(context, "Đã xóa thành công!", Toast.LENGTH_SHORT).show()
                                    loadMyDocuments() // Tải lại danh sách sau khi xóa
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi khi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { documentToDelete = null }) {
                        Text("Hủy", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun MyDocumentItem(document: Document, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF4C9EEB), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = document.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Đã tải lên: ${document.uploadDate.take(10)}", color = Color.Gray, fontSize = 12.sp)
                Text(text = document.size, color = Color(0xFF4C9EEB), fontSize = 12.sp)
            }
            // Nút Thùng rác
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}