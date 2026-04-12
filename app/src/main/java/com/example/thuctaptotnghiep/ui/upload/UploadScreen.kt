package com.example.thuctaptotnghiep.ui.upload

import android.content.Context
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thuctaptotnghiep.data.network.RetrofitClient
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun UploadScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- STATE QUẢN LÝ FILE ---
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // --- STATE CHO CÁC TRƯỜNG DỮ LIỆU MỚI ---
    var customTitle by remember { mutableStateOf("") }

    // CẬP NHẬT: Biến state đơn giản cho Môn học (thay cho Dropdown)
    var subject by remember { mutableStateOf("") }

    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    val categories = listOf("Slide", "Đề thi", "Giáo trình")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

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

                // Khung chọn file
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFFEBE1E1), RoundedCornerShape(16.dp))
                        .clickable { if (!isUploading) filePickerLauncher.launch("application/pdf") },
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
                            if (!isUploading) {
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
                        onValueChange = { customTitle = it },
                        label = { Text("Tiêu đề tài liệu *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // CẬP NHẬT: Ô nhập liệu Môn học dạng chữ
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
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
                        onValueChange = { description = it },
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
                            // CẬP NHẬT: Kiểm tra thêm điều kiện người dùng phải nhập Môn học
                            if (customTitle.isBlank() || subject.isBlank() || selectedFileUri == null) {
                                Toast.makeText(context, "Vui lòng nhập đủ Tiêu đề, Môn học và chọn file!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isUploading = true
                            coroutineScope.launch {
                                try {
                                    val tempFile = uriToFile(context, selectedFileUri!!)
                                    if (tempFile != null) {
                                        val requestFile = tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
                                        val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        val actualAuthorName = currentUser?.displayName ?: "Người dùng ẩn danh"

                                        val titlePart = customTitle.toRequestBody("text/plain".toMediaTypeOrNull())
                                        val authorPart = actualAuthorName.toRequestBody("text/plain".toMediaTypeOrNull())

                                        // Dùng biến subject gõ tay truyền vào API
                                        val subjectPart = subject.toRequestBody("text/plain".toMediaTypeOrNull())

                                        val categoryPart = selectedCategory.toRequestBody("text/plain".toMediaTypeOrNull())
                                        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                                        val tagsPart = tags.toRequestBody("text/plain".toMediaTypeOrNull())

                                        // Gọi API
                                        RetrofitClient.apiService.uploadDocument(
                                            body, titlePart, authorPart, subjectPart, categoryPart, descriptionPart, tagsPart
                                        )

                                        Toast.makeText(context, "Tải lên thành công rực rỡ!", Toast.LENGTH_SHORT).show()

                                        // Reset form
                                        selectedFileName = null
                                        selectedFileUri = null
                                        customTitle = ""
                                        subject = ""
                                        description = ""
                                        tags = ""
                                    } else {
                                        Toast.makeText(context, "Không thể đọc file", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi kết nối Server: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isUploading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !isUploading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FB1F0))
                    ) {
                        if (isUploading) {
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

suspend fun uriToFile(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
    val contentResolver = context.contentResolver
    val fileName = getFileName(context, uri) ?: "temp_pdf_file.pdf"
    val tempFile = File(context.cacheDir, fileName)

    try {
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return@withContext tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}