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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.InsertDriveFile
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
import com.google.firebase.auth.FirebaseAuth // ĐÃ THÊM IMPORT FIREBASE
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
    val coroutineScope = rememberCoroutineScope() // Để chạy tác vụ mạng dưới nền

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) } // Trạng thái đang tải

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = getFileName(context, it)
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

            // Nội dung chính
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(220.dp))

                Text("Upload tài liệu", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chia sẻ tài liệu của Bạn bằng cách\nUpload file để mọi người có thể\nxem, tải và kết nối cùng Bạn.",
                    fontSize = 15.sp, color = Color.DarkGray, textAlign = TextAlign.Center, lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(40.dp))

                // Khung chọn file
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
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
                                Text("Chọn file", fontSize = 18.sp, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Kéo & thả tài liệu vào đây\nhoặc bấm để chọn", textAlign = TextAlign.Center, color = Color.DarkGray, fontSize = 14.sp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = Color(0xFF6FB1F0), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(selectedFileName!!, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(16.dp))
                            if (!isUploading) {
                                Button(
                                    onClick = { selectedFileName = null; selectedFileUri = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                    shape = RoundedCornerShape(12.dp), modifier = Modifier.height(40.dp)
                                ) {
                                    Text("Chọn lại file khác", fontSize = 14.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Nút Tải lên và Loading logic
                if (selectedFileName != null) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (selectedFileUri != null) {
                                isUploading = true
                                coroutineScope.launch {
                                    try {
                                        val tempFile = uriToFile(context, selectedFileUri!!)
                                        if (tempFile != null) {
                                            val requestFile = tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
                                            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                                            // ĐÃ CẬP NHẬT: LẤY TÊN THẬT TỪ FIREBASE
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            val actualAuthorName = currentUser?.displayName ?: "Người dùng ẩn danh"

                                            val titlePart = (selectedFileName ?: "Tài liệu").toRequestBody("text/plain".toMediaTypeOrNull())
                                            val authorPart = actualAuthorName.toRequestBody("text/plain".toMediaTypeOrNull()) // Truyền tên thật

                                            val response = RetrofitClient.apiService.uploadDocument(body, titlePart, authorPart)

                                            Toast.makeText(context, "Tải lên thành công rực rỡ!", Toast.LENGTH_SHORT).show()

                                            selectedFileName = null
                                            selectedFileUri = null
                                        } else {
                                            Toast.makeText(context, "Không thể đọc file", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Lỗi kết nối Server: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isUploading = false
                                    }
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
                }
            }
        }
    }
}

// -------------------------------------------------------------------
// CÁC HÀM HỖ TRỢ XỬ LÝ FILE
// -------------------------------------------------------------------

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