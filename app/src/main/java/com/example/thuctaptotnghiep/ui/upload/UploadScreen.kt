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
import androidx.compose.material3.*
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
    viewModel: UploadViewModel = hiltViewModel() // Bơm ViewModel vào bằng Hilt
) {
    val context = LocalContext.current

    // --- LẮNG NGHE TRẠNG THÁI TỪ VIEWMODEL ---
    val uploadState by viewModel.uploadState.collectAsState()

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
        when (uploadState) {
            is UploadState.Success -> {
                Toast.makeText(context, "Tải lên thành công rực rỡ!", Toast.LENGTH_SHORT).show()
                // Reset form sau khi upload xong
                selectedFileName = null
                selectedFileUri = null
                customTitle = ""
                subject = ""
                description = ""
                tags = ""
                viewModel.resetState() // Trả về trạng thái Idle chờ lượt upload tiếp
            }
            is UploadState.Error -> {
                val errorMessage = (uploadState as UploadState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            // Lấy tên file tạm thời để hiển thị (Logic lấy tên thật được đưa xuống dưới)
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

                    // ==========================================
                    // FORM NHẬP THÔNG TIN TÀI LIỆU
                    // ==========================================
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = {
                            if (it.length <= 100) customTitle = it
                        },
                        label = { Text("Tiêu đề tài liệu *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = {
                            if (it.length <= 50) subject = it
                        },
                        label = { Text("Môn học *") },
                        placeholder = { Text("VD: Toán cao cấp, Lập trình Android...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6FB1F0))
                                    )
                                    Text(text = category, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= 500) description = it
                        },
                        label = { Text("Mô tả nội dung") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (Cách nhau dấu phẩy)") },
                        placeholder = { Text("VD: hk1, kho, ck") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // ==========================================
                    // NÚT TẢI LÊN
                    // ==========================================
                    Button(
                        onClick = {
                            if (customTitle.isBlank() || subject.isBlank() || selectedFileUri == null) {
                                Toast.makeText(context, "Vui lòng nhập đủ Tiêu đề, Môn học và chọn file!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val actualAuthorName = currentUser?.displayName ?: "Người dùng ẩn danh"

                            // GIAO VIỆC CHO VIEWMODEL LÀM
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
                        enabled = uploadState !is UploadState.Loading, // Khóa nút khi đang tải
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FB1F0))
                    ) {
                        if (uploadState is UploadState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("ĐANG TẢI LÊN...", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Text("TẢI LÊN NGAY", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

// Hàm lấy tên file để hiển thị trên UI
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