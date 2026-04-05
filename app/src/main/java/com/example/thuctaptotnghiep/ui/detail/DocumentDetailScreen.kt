package com.example.thuctaptotnghiep.ui.detail

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
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
import androidx.lifecycle.viewmodel.compose.viewModel // Import MVVM
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState



@Composable
fun DocumentDetailScreen(
    documentId: String,
    viewModel: DetailViewModel = viewModel(), // <-- TIÊM VIEWMODEL VÀO ĐÂY
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current

    // "LẮNG NGHE" DỮ LIỆU TỪ VIEWMODEL
    val document by viewModel.document.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Biến trạng thái để mở giao diện xem PDF toàn màn hình
    var isPreviewOpen by remember { mutableStateOf(false) }

    // Tự động lấy dữ liệu khi ID thay đổi
    LaunchedEffect(documentId) {
        if (documentId.isNotEmpty()) {
            viewModel.fetchDocumentDetail(documentId)
        }
    }

    // Hiển thị thông báo nếu có lỗi từ Server
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_SHORT).show()
        }
    }

    // Xử lý nút Back của điện thoại: Nếu đang mở PDF thì đóng PDF lại, chứ không thoát hẳn ra ngoài
    BackHandler(enabled = isPreviewOpen) {
        isPreviewOpen = false
    }

    // =====================================================================
    // GIAO DIỆN XEM TRƯỚC PDF (FULL SCREEN)
    // =====================================================================
    if (isPreviewOpen && document != null) {
        val fullUrl = "http://10.0.2.2:3000${document!!.fileUrl}"

        // Khởi tạo bộ nhớ cho thư viện đọc PDF
        val pdfState = rememberVerticalPdfReaderState(
            resource = ResourceType.Remote(fullUrl),
            isZoomEnable = true
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Màn hình lật trang PDF
            VerticalPDFReader(
                state = pdfState,
                modifier = Modifier.fillMaxSize()
            )

            // Thanh công cụ phía trên (Nút Đóng)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isPreviewOpen = false }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = document!!.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
    // =====================================================================
    // GIAO DIỆN CHI TIẾT TÀI LIỆU BÌNH THƯỜNG
    // =====================================================================
    else {
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(onHomeClick, onUploadClick, onProfileClick, onSearchClick)
            },
            containerColor = Color(0xFFF5F5F5)
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF4C9EEB))
                } else if (document != null) {
                    val doc = document!!

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        // Header Ảnh bìa và nút Back
                        Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(Color(0xFFE3F2FD))) {
                            Box(
                                modifier = Modifier.padding(top = 40.dp, start = 20.dp).size(44.dp)
                                    .background(Color.White.copy(alpha = 0.8f), CircleShape).clip(CircleShape).clickable { onBackClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                            }
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Cover", tint = Color(0xFF4C9EEB), modifier = Modifier.size(100.dp).align(Alignment.Center))
                        }

                        // Phần Thông tin sách
                        Column(
                            modifier = Modifier.offset(y = (-20).dp).fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).padding(24.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Text(text = doc.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Gray, modifier = Modifier.padding(start = 16.dp).clickable { /* TODO */ })
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF4C9EEB)), contentAlignment = Alignment.Center) {
                                    Text(doc.authorName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Đăng bởi: ${doc.authorName}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Cập nhật: ${doc.uploadDate.take(10)}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)).padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                InfoItem("Dung lượng", doc.size)
                                InfoItem("Lượt xem", "${doc.views}")
                                InfoItem("Lượt tải", "${doc.downloads}")
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // ==========================================
                            // NÚT XEM TRƯỚC VÀ NÚT TẢI VỀ
                            // ==========================================
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Nút Xem trước
                                Button(
                                    onClick = { isPreviewOpen = true },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB))
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("XEM TRƯỚC", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }

                                // Nút Tải về
                                Button(
                                    onClick = { downloadDocument(context, doc.fileUrl, doc.title) },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF286090))
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("TẢI VỀ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Text("Không tìm thấy dữ liệu", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

// Hàm tải xuống giữ nguyên
fun downloadDocument(context: Context, fileUrl: String, title: String) {
    try {
        val fullUrl = "http://10.0.2.2:3000$fileUrl"
        val request = DownloadManager.Request(Uri.parse(fullUrl))
            .setTitle(title)
            .setDescription("Đang tải tài liệu từ ứng dụng...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$title.pdf")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, "Bắt đầu tải xuống...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Lỗi khi tải: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}