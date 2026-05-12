package com.example.thuctaptotnghiep.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thuctaptotnghiep.R
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    // Danh sách các trang Onboarding
    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.intro1,
            title = "Tìm kiếm thông minh, đúng chuyên ngành",
            description = "Truy cập kho tài liệu khổng lồ từ mọi khoa và môn học tại UTH. Tìm kiếm đề thi, slide bài giảng, và ghi chú chỉ trong vài giây."
        ),
        OnboardingPage(
            imageRes = R.drawable.intro2,
            title = "Chia sẻ tri thức, xây dựng cộng đồng",
            description = "Trở thành một phần của cộng đồng sinh viên UTH. Đăng tải tài liệu của bạn, giúp đỡ bạn bè và tích lũy điểm đóng góp."
        ),
        OnboardingPage(
            imageRes = R.drawable.intro3,
            title = "Ôn tập hiệu quả, chinh phục điểm cao",
            description = "Tất cả tài liệu bạn cần cho các kỳ thi đều ở đây. Hãy học tập thông minh hơn và sẵn sàng cho mọi thử thách."
        )
    )

    // [CẬP NHẬT]: Cú pháp mới của Compose Foundation Pager
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding() // Thêm cái này để không bị lẹm vào thanh trạng thái (pin, wifi)
    ) {
        // Nút "Bỏ qua" ở góc trên cùng bên phải
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = "Bỏ qua",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onFinishOnboarding() }
                    .padding(8.dp),
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Pager chứa các hình ảnh và nội dung
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { position ->
            OnboardingPageContent(page = pages[position])
        }

        // ==========================================
        // [CẬP NHẬT]: Dots indicator tự tùy biến (Không cần thư viện)
        // ==========================================
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val color = if (isSelected) Color(0xFF4C9EEB) else Color(0xFFE2E8F0)
                val width = if (isSelected) 24.dp else 10.dp // Chấm đang chọn sẽ kéo dài ra cho đẹp

                Box(
                    modifier = Modifier
                        .size(width = width, height = 10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        // Nút điều hướng (Tiếp tục / Bắt đầu)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if (pagerState.currentPage == pages.size - 1) {
                        onFinishOnboarding()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "Bắt đầu" else "Tiếp Tục",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp), // Phóng to ảnh lên chút cho đẹp
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = page.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            fontSize = 15.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp // Giãn dòng cho dễ đọc
        )
    }
}

// Data class đại diện cho 1 trang Onboarding
data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)