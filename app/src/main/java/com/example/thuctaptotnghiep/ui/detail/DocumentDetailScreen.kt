package com.example.thuctaptotnghiep.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar // Import thanh Bottom Bar

@Composable
fun DocumentDetailScreen(
    documentTitle: String,
    onBackClick: () -> Unit,
    // THÊM CÁC THAM SỐ CHUYỂN TRANG CHO BOTTOM BAR
    onHomeClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            AppBottomNavigationBar(
                onHomeClick = onHomeClick,
                onUploadClick = onUploadClick,
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // 1. Nền lượn sóng màu xanh
            Canvas(modifier = Modifier.fillMaxWidth().height(260.dp).align(Alignment.TopCenter)) {
                val path = Path().apply {
                    lineTo(0f, size.height - 80f)
                    quadraticBezierTo(size.width * 0.5f, size.height, size.width, size.height - 80f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, Color(0xFF6FB1F0))
            }

            // Toàn bộ nội dung cuộn được
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar (Nút Back và Nút 3 chấm nằm trong vòng tròn trắng)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).background(Color.White, CircleShape).clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                    Text("Chi tiết tài liệu", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Box(
                        modifier = Modifier.size(44.dp).background(Color.White, CircleShape).clickable { /* TODO: Menu */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "Thêm")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Khu vực ảnh bìa đè lên khung xám
                Box(modifier = Modifier.fillMaxWidth()) {

                    // Khung xám nhạt bo tròn góc trên
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 110.dp) // Đẩy khung xám xuống nửa chiều cao ảnh bìa
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(130.dp)) // Nhường chỗ cho nửa dưới ảnh bìa

                        // Người đăng
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Color.LightGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Người đăng", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Khung trắng chứa Icon Đánh giá, Tải về, Lượt xem
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color.White, RoundedCornerShape(28.dp))
                                .padding(horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Đánh giá", tint = Color(0xFFFFC107), modifier = Modifier.size(28.dp))
                            Icon(Icons.Default.Download, contentDescription = "Tải về", tint = Color.Black, modifier = Modifier.size(28.dp))
                            Icon(Icons.Default.Visibility, contentDescription = "Lượt xem", tint = Color.Black, modifier = Modifier.size(28.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nút Tải Về
                        Button(
                            onClick = { /* TODO: Xử lý tải PDF */ },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FB1F0))
                        ) {
                            Text("Tải Về", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Các Section mô tả chi tiết
                        DetailSection("Mô tả")
                        Text(
                            text = "Đây là tài liệu: $documentTitle. Nội dung bao gồm toàn bộ bài giảng và bài tập thực hành. Rất phù hợp cho việc ôn tập cuối kỳ.",
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        DetailSection("Thông tin thêm")
                        Text("Định dạng: PDF\nDung lượng: 2.5 MB\nNgôn ngữ: Tiếng Việt", color = Color.DarkGray, lineHeight = 22.sp)

                        Spacer(modifier = Modifier.height(24.dp))

                        DetailSection("Bình luận")

                        // Khung viết bình luận
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp) // Thêm khoảng trống chống lẹm Bottom Bar
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Color.LightGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(Color.White, RoundedCornerShape(24.dp))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text("Viết bình luận của bạn", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }

                    // Hình ảnh bìa sách nổi lên trên
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .width(160.dp)
                            .height(220.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

// Widget vẽ đường kẻ ngang và Tiêu đề mục (Giống Figma)
@Composable
fun DetailSection(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        HorizontalDivider(modifier = Modifier.width(16.dp), color = Color.Gray)
        Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp), fontSize = 16.sp)
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray)
    }
}