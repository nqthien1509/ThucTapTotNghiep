package com.example.thuctaptotnghiep.ui.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar

@Composable
fun UploadScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current

    // Biến lưu trữ tên file đã chọn
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    // Biến lưu trữ Uri của file để sau này upload lên Server
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    // Bộ phóng (Launcher) để mở trình quản lý tệp của hệ điều hành
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Khi người dùng chọn file xong, code này sẽ chạy
        uri?.let {
            selectedFileUri = it
            selectedFileName = getFileName(context, it) // Gọi hàm lấy tên file
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            AppBottomNavigationBar(
                onHomeClick = onHomeClick,
                onUploadClick = { /* Đang ở màn hình Upload rồi nên không làm gì */ },
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // 1. Vẽ nền lượn sóng màu xanh dương nhạt (Header)
            Canvas(modifier = Modifier.fillMaxWidth().height(200.dp).align(Alignment.TopCenter)) {
                val path = Path().apply {
                    lineTo(0f, size.height - 60f)
                    quadraticBezierTo(size.width * 0.4f, size.height + 20f, size.width, size.height - 80f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, Color(0xFF6FB1F0))
            }

            // Nút Back tròn nền trắng
            Box(
                modifier = Modifier
                    .padding(top = 40.dp, start = 20.dp)
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .clip(CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.Black)
            }

            // 2. Nội dung chính
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(220.dp))

                Text(
                    text = "Upload tài liệu",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Chia sẻ tài liệu của Bạn bằng cách\nUpload file để mọi người có thể\nxem, tải và kết nối cùng Bạn.",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 3. Khung Upload có tương tác thật
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFEBE1E1), RoundedCornerShape(16.dp))
                        .clickable {
                            // Bấm vào Box này cũng mở trình chọn file (Lọc lấy file PDF)
                            filePickerLauncher.launch("application/pdf")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (selectedFileName == null) {
                            // TRẠNG THÁI 1: CHƯA CHỌN FILE
                            Button(
                                onClick = {
                                    // Bấm nút mở trình chọn file PDF
                                    filePickerLauncher.launch("application/pdf")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FB1F0)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Chọn file", fontSize = 18.sp, color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Kéo & thả tài liệu vào đây\nhoặc bấm để chọn",
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        } else {
                            // TRẠNG THÁI 2: ĐÃ CHỌN FILE THÀNH CÔNG
                            Icon(
                                Icons.Default.InsertDriveFile,
                                contentDescription = "File Icon",
                                tint = Color(0xFF6FB1F0),
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = selectedFileName!!,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    // Hủy file đã chọn
                                    selectedFileName = null
                                    selectedFileUri = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)), // Nút màu đỏ
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("Chọn lại file khác", fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Nếu đã chọn file, hiện thêm nút TẢI LÊN màu xanh khổng lồ ở dưới
                if (selectedFileName != null) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            /* TODO: Gọi Backend đẩy file lên mạng */
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FB1F0))
                    ) {
                        Text("TẢI LÊN NGAY", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// Hàm hỗ trợ dịch đường dẫn nội bộ (Uri) của Android thành tên file hiển thị dễ đọc
fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}