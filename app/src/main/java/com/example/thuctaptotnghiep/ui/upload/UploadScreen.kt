package com.example.thuctaptotnghiep.ui.upload

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CheckCircle // Bổ sung icon Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UploadScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit, // CẢI TIẾN: Thêm tham số điều hướng đến Detail
    viewModel: UploadViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // --- LẮNG NGHE TRẠNG THÁI TỪ VIEWMODEL ---
    val uploadState by viewModel.uploadState.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState() // CẢI TIẾN: Lắng nghe % upload

    // --- STATE QUẢN LÝ FILE VÀ FORM ---
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    var customTitle by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    val categories = listOf("Slide", "Đề thi", "Giáo trình")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // --- XỬ LÝ TOAST THÔNG BÁO TỪ TRẠNG THÁI ---
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
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            AppBottomNavigationBar(
                onHomeClick = onHomeClick,
                onUploadClick = { },
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Header nền sóng xanh
            Canvas(modifier = Modifier.fillMaxWidth().height(200.dp).align(Alignment.TopCenter)) {
                val path = Path().apply {
                    lineTo(0f, size.height - 60f)
                    quadraticTo(size.width * 0.4f, size.height + 20f, size.width, size.height - 80f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, Color(0xFF6FB1F0))
            }

            // Nút Back
            Box(
                modifier = Modifier
                    .padding(top = 40.dp, start = 20.dp)
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .clip(CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.Black)
            }

            // NỘI DUNG CHÍNH
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(200.dp))

                Text("Upload tài liệu", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                // CẢI TIẾN: Xử lý hiển thị UI dựa trên State
                if (uploadState is UploadState.Success) {
                    // --- MÀN HÌNH THÀNH CÔNG (CTA) ---
                    val successState = uploadState as UploadState.Success
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(72.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Đăng tài liệu thành công!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(24.dp))

                            // Nút Xem tài liệu
                            Button(
                                onClick = { onNavigateToDetail(successState.documentId) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Xem tài liệu vừa đăng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Nút Đăng tiếp
                            OutlinedButton(
                                onClick = {
                                    selectedFileName = null
                                    selectedFileUri = null
                                    customTitle = ""
                                    subject = ""
                                    description = ""
                                    tags = ""
                                    viewModel.resetState()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Tải lên tệp khác", fontSize = 16.sp, color = Color.DarkGray)
                            }
                        }
                    }
                } else {
                    // --- FORM UPLOAD HIỆN TẠI ---

                    // Khung chọn file
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFFEBE1E1), RoundedCornerShape(16.dp))
                            .clickable { if (uploadState !is UploadState.Loading) filePickerLauncher.launch("application/pdf") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            if (selectedFileName == null) {
                                Button(
                                    onClick = { filePickerLauncher.launch("application/pdf") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FB1F0)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Chọn file PDF", fontSize = 18.sp, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Bấm để chọn tài liệu từ thiết bị", color = Color.DarkGray, fontSize = 14.sp)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = Color(0xFF6FB1F0), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(selectedFileName!!, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(16.dp))
                                if (uploadState !is UploadState.Loading) {
                                    TextButton(onClick = { selectedFileName = null; selectedFileUri = null; customTitle = "" }) {
                                        Text("Đổi file khác", color = Color.Red)
                                    }
                                }
                            }
                        }
                    }

                    if (selectedFileName != null) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Form nhập liệu
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { if (it.length <= 100) customTitle = it },
                            label = { Text("Tiêu đề tài liệu *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = uploadState !is UploadState.Loading
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = subject,
                            onValueChange = { if (it.length <= 50) subject = it },
                            label = { Text("Môn học *") },
                            placeholder = { Text("VD: Toán cao cấp...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = uploadState !is UploadState.Loading
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Loại tài liệu *", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                            Row(
                                modifier = Modifier.selectableGroup().fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                categories.forEach { category ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = (selectedCategory == category),
                                            onClick = { selectedCategory = category },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6FB1F0)),
                                            enabled = uploadState !is UploadState.Loading
                                        )
                                        Text(text = category, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 500) description = it },
                            label = { Text("Mô tả nội dung") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 3,
                            enabled = uploadState !is UploadState.Loading
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Tags (Cách nhau dấu phẩy)") },
                            placeholder = { Text("VD: hk1, kho, ck") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = uploadState !is UploadState.Loading
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // CẢI TIẾN: Giao diện khi đang Loading (Thanh Tiến Trình)
                        if (uploadState is UploadState.Loading) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Text(
                                    "Đang tải lên... ${(uploadProgress * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6FB1F0)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress },
                                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                                    color = Color(0xFF6FB1F0),
                                    trackColor = Color.LightGray
                                )
                            }
                        } else {
                            // Nút Tải lên mặc định
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
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FB1F0))
                            ) {
                                Text("TẢI LÊN NGAY", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

fun getFileName(context: android.content.Context, uri: Uri): String? {
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