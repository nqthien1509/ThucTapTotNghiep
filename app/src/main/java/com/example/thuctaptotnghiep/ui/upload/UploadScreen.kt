package com.example.thuctaptotnghiep.ui.upload

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.statusBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val uploadState by viewModel.uploadState.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var pdfThumbnail by remember { mutableStateOf<Bitmap?>(null) }

    var customTitle by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    val categories = listOf("Slide", "Đề thi", "Giáo trình")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    LaunchedEffect(uploadState) {
        if (uploadState is UploadState.Error) {
            val errorMessage = (uploadState as UploadState.Error).message
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val name = getFileName(context, it)
            selectedFileName = name
            if (customTitle.isBlank() && name != null) {
                customTitle = name.substringBeforeLast(".")
            }
            pdfThumbnail = generatePdfThumbnail(context, it)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA), // Nền sáng
        bottomBar = {
            AppBottomNavigationBar(
                currentRoute = "upload", // Đặt trạng thái tab Upload đang mở
                onHomeClick = onHomeClick,
                onUploadClick = { },
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // [CẬP NHẬT UI]: Header Gradient bo sâu hiện đại
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        brush = Brush.verticalGradient(listOf(Color(0xFF4C9EEB), Color(0xFF1E88E5))),
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    )
                    .shadow(4.dp, RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.statusBarsPadding().padding(top = 16.dp, start = 16.dp)
                ) {
                    Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                Text("Chia sẻ tài liệu", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Lan tỏa tri thức, nhận ngàn yêu thương", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(32.dp))

                // Trạng thái Success
                if (uploadState is UploadState.Success) {
                    val successState = uploadState as UploadState.Success
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x334CAF50)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(80.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tải lên thành công!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tài liệu của bạn đang được duyệt.", fontSize = 14.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = { onNavigateToDetail(successState.documentId) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("XEM TÀI LIỆU CỦA BẠN", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    selectedFileName = null; selectedFileUri = null; pdfThumbnail = null; customTitle = ""; subject = ""; description = ""; tags = ""; viewModel.resetState()
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text("TẢI LÊN FILE KHÁC", fontSize = 15.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // [CẬP NHẬT UI]: Khu vực kéo thả / Chọn file có viền đứt nét mướt mắt
                    val stroke = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4C9EEB).copy(alpha = 0.5f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1A000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(stroke, RoundedCornerShape(24.dp))
                            .clickable { if (uploadState !is UploadState.Loading) filePickerLauncher.launch("application/pdf") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            if (selectedFileName == null) {
                                Box(
                                    modifier = Modifier.size(72.dp).background(Color(0xFFE3F2FD), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = Color(0xFF4C9EEB), modifier = Modifier.size(36.dp))
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text("Nhấn để tải lên file PDF", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Dung lượng tối đa 50MB", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            } else {
                                if (pdfThumbnail != null) {
                                    Image(
                                        bitmap = pdfThumbnail!!.asImageBitmap(),
                                        contentDescription = "PDF Preview",
                                        modifier = Modifier.height(110.dp).shadow(4.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.FillHeight
                                    )
                                } else {
                                    Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = Color(0xFF4C9EEB), modifier = Modifier.size(64.dp))
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(selectedFileName!!, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                                if (uploadState !is UploadState.Loading) {
                                    TextButton(onClick = {
                                        selectedFileName = null; selectedFileUri = null; customTitle = ""; pdfThumbnail = null
                                    }) {
                                        Text("Thay đổi file", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (selectedFileName != null) {
                        Spacer(modifier = Modifier.height(32.dp))

                        // [CẬP NHẬT UI]: Form nhập liệu mượt mà, bo góc
                        val textFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF4C9EEB),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )

                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { if (it.length <= 100) customTitle = it },
                            label = { Text("Tiêu đề tài liệu *", color = Color(0xFF64748B)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = uploadState !is UploadState.Loading,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = subject,
                            onValueChange = { if (it.length <= 50) subject = it },
                            label = { Text("Môn học *", color = Color(0xFF64748B)) },
                            placeholder = { Text("VD: Toán cao cấp...", color = Color(0xFFCBD5E1)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = uploadState !is UploadState.Loading,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Loại tài liệu", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.selectableGroup().fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                categories.forEach { category ->
                                    val isSelected = selectedCategory == category
                                    Surface(
                                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(0xFF4C9EEB) else Color(0xFFF1F5F9),
                                        onClick = { if (uploadState !is UploadState.Loading) selectedCategory = category }
                                    ) {
                                        Text(
                                            text = category,
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF64748B),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 500) description = it },
                            label = { Text("Mô tả nội dung", color = Color(0xFF64748B)) },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            maxLines = 4,
                            enabled = uploadState !is UploadState.Loading,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Tags (Cách nhau dấu phẩy)", color = Color(0xFF64748B)) },
                            placeholder = { Text("VD: hk1, khtn", color = Color(0xFFCBD5E1)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = uploadState !is UploadState.Loading,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        if (uploadState is UploadState.Loading) {
                            // [CẬP NHẬT UI]: Progress Bar hiện đại
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                            ) {
                                Text(
                                    "Đang xử lý tài liệu... ${(uploadProgress * 100).toInt()}%",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4C9EEB),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress },
                                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                                    color = Color(0xFF4C9EEB),
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (customTitle.isBlank() || subject.isBlank() || selectedFileUri == null) {
                                        Toast.makeText(context, "Vui lòng nhập đủ Tiêu đề, Môn học và chọn file!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val currentUser = FirebaseAuth.getInstance().currentUser
                                    val actualAuthorName = currentUser?.displayName ?: "Người dùng ẩn danh"

                                    viewModel.uploadDocument(
                                        context = context,
                                        uri = selectedFileUri!!,
                                        title = customTitle,
                                        authorName = actualAuthorName,
                                        subject = subject,
                                        category = selectedCategory,
                                        description = description,
                                        tags = tags
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(60.dp).shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0x404C9EEB)),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB))
                            ) {
                                Text("TẢI LÊN NGAY BÂY GIỜ", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

// Giữ nguyên các hàm tiện ích
fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor.use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) result = cursor.getString(index)
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) result = result?.substring(cut + 1)
    }
    return result
}

fun generatePdfThumbnail(context: Context, pdfUri: Uri): Bitmap? {
    var fileDescriptor: ParcelFileDescriptor? = null
    var pdfRenderer: PdfRenderer? = null
    var currentPage: PdfRenderer.Page? = null

    return try {
        fileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
        if (fileDescriptor != null) {
            pdfRenderer = PdfRenderer(fileDescriptor)
            if (pdfRenderer.pageCount > 0) {
                currentPage = pdfRenderer.openPage(0)
                val bitmap = Bitmap.createBitmap(
                    currentPage.width,
                    currentPage.height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.eraseColor(android.graphics.Color.WHITE)
                currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            } else null
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        currentPage?.close()
        pdfRenderer?.close()
        fileDescriptor?.close()
    }
}