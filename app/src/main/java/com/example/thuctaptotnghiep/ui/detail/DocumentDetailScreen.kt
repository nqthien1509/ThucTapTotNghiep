package com.example.thuctaptotnghiep.ui.detail

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.example.thuctaptotnghiep.utils.toFullUrl
import com.google.firebase.auth.FirebaseAuth
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRequireLogin: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val document by viewModel.document.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isWatchLater by viewModel.isWatchLater.collectAsState()
    val isTogglingAction by viewModel.isTogglingAction.collectAsState()

    var isPreviewOpen by remember { mutableStateOf(false) }

    // ==========================================
    // STATE CHO TÍNH NĂNG BÁO CÁO (REPORT)
    // ==========================================
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    // Biến & Hàm kiểm tra đăng nhập tại chỗ
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val performAuthAction = { action: () -> Unit ->
        if (isLoggedIn) action() else onRequireLogin()
    }

    LaunchedEffect(documentId) {
        if (documentId.isNotEmpty()) {
            viewModel.fetchDocumentDetail(documentId)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler(enabled = isPreviewOpen) { isPreviewOpen = false }

    if (isPreviewOpen && document != null) {
        val fullUrl = document!!.fileUrl.toFullUrl()
        val pdfState = rememberVerticalPdfReaderState(
            resource = ResourceType.Remote(fullUrl),
            isZoomEnable = true
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            VerticalPDFReader(state = pdfState, modifier = Modifier.fillMaxSize())
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
    } else {
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(
                    currentRoute = null,
                    onHomeClick = onHomeClick,
                    onUploadClick = onUploadClick,
                    onProfileClick = onProfileClick,
                    onSearchClick = onSearchClick
                )
            },
            containerColor = Color(0xFFF8F9FA)
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF4C9EEB))
                } else if (document != null) {
                    val doc = document!!

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                                    )
                                )
                        ) {
                            if (!doc.thumbnailUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = doc.thumbnailUrl.toFullUrl(),
                                    contentDescription = "Ảnh bìa tài liệu",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 60.dp, bottom = 40.dp)
                                        .shadow(12.dp, RoundedCornerShape(8.dp))
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF4C9EEB).copy(alpha = 0.4f),
                                    modifier = Modifier.size(120.dp).align(Alignment.Center)
                                )
                            }

                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .padding(top = 40.dp, start = 16.dp)
                                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .offset(y = (-30).dp)
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                                .padding(24.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = doc.title,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.weight(1f),
                                    lineHeight = 32.sp
                                )

                                Row {
                                    // Nút Lưu Xem Sau
                                    ActionIconButton(
                                        icon = if (isWatchLater) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        activeColor = Color(0xFF4C9EEB),
                                        isActive = isWatchLater,
                                        onClick = { performAuthAction { viewModel.toggleWatchLater(doc.id) } }
                                    )
                                    // Nút Yêu Thích
                                    ActionIconButton(
                                        icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        activeColor = Color.Red,
                                        isActive = isFavorite,
                                        onClick = { performAuthAction { viewModel.toggleFavorite(doc.id) } }
                                    )
                                    // [THÊM MỚI] Nút Báo Cáo
                                    ActionIconButton(
                                        icon = Icons.Rounded.Warning,
                                        activeColor = Color.Transparent,
                                        isActive = false, // Không đổi màu khi click như toggle
                                        onClick = { performAuthAction { showReportDialog = true } }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = doc.category ?: "Tài liệu",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = Color(0xFF2E7D32),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = doc.subject ?: "Chưa phân loại",
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(doc.authorName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF4C9EEB), fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(doc.authorName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    Text("Ngày đăng: ${doc.uploadDate.take(10)}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                // Nút chia sẻ thì không cần bắt đăng nhập
                                IconButton(onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Đọc tài liệu '${doc.title}' trên StuShare: http://stushare.com/doc/${doc.id}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF4C9EEB))
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ModernStatItem(Modifier.weight(1f), "Dung lượng", doc.size ?: "N/A", Icons.Rounded.Storage)
                                ModernStatItem(Modifier.weight(1f), "Lượt xem", "${doc.views ?: 0}", Icons.Rounded.Visibility)
                                ModernStatItem(Modifier.weight(1f), "Lượt tải", "${doc.downloads ?: 0}", Icons.Rounded.FileDownload)
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            if (!doc.description.isNullOrBlank()) {
                                Text("Mô tả tài liệu", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = doc.description!!,
                                    fontSize = 15.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 24.sp,
                                    textAlign = TextAlign.Justify
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = {
                                        performAuthAction {
                                            viewModel.incrementViewCount(doc.id)
                                            isPreviewOpen = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Icon(Icons.Rounded.Visibility, contentDescription = null, tint = Color(0xFF1E293B))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("XEM TRƯỚC", color = Color(0xFF1E293B), fontWeight = FontWeight.ExtraBold)
                                }

                                Button(
                                    onClick = {
                                        performAuthAction {
                                            val safeFileName = viewModel.getSafeFileName(doc.title)
                                            downloadDocument(context, doc.fileUrl, safeFileName)
                                            viewModel.incrementDownloadCount(doc.id)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(58.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB)),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                                ) {
                                    Icon(Icons.Rounded.FileDownload, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("TẢI VỀ", color = Color.White, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }

                    // ==========================================
                    // GIAO DIỆN DIALOG BÁO CÁO TÀI LIỆU
                    // ==========================================
                    if (showReportDialog) {
                        AlertDialog(
                            onDismissRequest = { showReportDialog = false; reportReason = "" },
                            title = { Text("Báo cáo tài liệu", fontWeight = FontWeight.Bold) },
                            text = {
                                Column {
                                    Text(
                                        "Bạn đang báo cáo tài liệu: ${doc.title}. Vui lòng cho biết lý do (ví dụ: nội dung sai lệch, vi phạm bản quyền, spam...):",
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = reportReason,
                                        onValueChange = { reportReason = it },
                                        placeholder = { Text("Nhập lý do báo cáo...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFEF4444)
                                        )
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (reportReason.isNotBlank()) {
                                            viewModel.reportDocument(
                                                targetId = doc.id,
                                                reason = reportReason,
                                                evidenceLink = doc.id, // Dùng ID làm bằng chứng luôn
                                                onSuccess = { msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                    showReportDialog = false
                                                    reportReason = ""
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        } else {
                                            Toast.makeText(context, "Vui lòng nhập lý do!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                ) {
                                    Text("Gửi báo cáo")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showReportDialog = false; reportReason = "" }) {
                                    Text("Hủy", color = Color.Gray)
                                }
                            }
                        )
                    }
                } else {
                    Text("Không tìm thấy dữ liệu", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun ActionIconButton(icon: ImageVector, activeColor: Color, isActive: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(start = 4.dp).background(if (isActive) activeColor.copy(alpha = 0.1f) else Color.Transparent, CircleShape)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isActive) activeColor else Color(0xFF94A3B8))
    }
}

@Composable
fun ModernStatItem(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Surface(modifier = modifier, color = Color(0xFFF8F9FA), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) {
        Column(modifier = Modifier.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
        }
    }
}

fun downloadDocument(context: Context, fileUrl: String, safeFileName: String) {
    try {
        val fullUrl = fileUrl.toFullUrl()
        val request = DownloadManager.Request(Uri.parse(fullUrl))
            .setTitle(safeFileName)
            .setDescription("Đang tải tài liệu...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeFileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Bắt đầu tải xuống...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}