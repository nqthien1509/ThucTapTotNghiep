package com.example.thuctaptotnghiep.ui.home

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.example.thuctaptotnghiep.ui.components.EmptyStateView
import com.example.thuctaptotnghiep.utils.UserManager
import com.example.thuctaptotnghiep.utils.toFullUrl

// =========================================================================
// EXTENSION TẠO HIỆU ỨNG SHIMMER (LẤP LÁNH CHUYỂN ĐỘNG)
// =========================================================================
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "shimmer_offset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE2E8F0), // Xám nhạt
                Color(0xFFF1F5F9), // Trắng xám (Tạo độ lóa sáng)
                Color(0xFFE2E8F0)  // Xám nhạt
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToUpload: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToSeeAll: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onLeaderboardClick: () -> Unit, // [THÊM MỚI]: Hàm xử lý khi bấm nút BXH
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val latestDocs by viewModel.latestDocs.collectAsState()
    val popularDocs by viewModel.popularDocs.collectAsState()
    val recommendedDocs by viewModel.recommendedDocs.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val currentUserProfile by UserManager.userProfile.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                currentRoute = "home",
                onHomeClick = {  },
                onUploadClick = onNavigateToUpload,
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick,
                onNotificationClick = onNotificationClick
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshDocuments() },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    HeaderSection(
                        userProfile = currentUserProfile,
                        fallbackName = viewModel.userName,
                        onSearchClick = onSearchClick,
                        onNotificationClick = onNotificationClick,
                        onCommunityClick = onCommunityClick,
                        onLeaderboardClick = onLeaderboardClick // [CẬP NHẬT]
                    )
                }

                // THAY THẾ LOADING BẰNG SKELETON
                if (isLoading && !isRefreshing) {
                    item { SkeletonDocumentSection() }
                    item { SkeletonDocumentSection() }
                    item { SkeletonDocumentSection() }
                } else {
                    item {
                        DocumentSection(
                            title = "Mới được tải lên",
                            items = latestDocs,
                            onItemClick = onDocumentClick,
                            onSeeAllClick = { onNavigateToSeeAll("LATEST") }
                        )
                    }
                    item {
                        DocumentSection(
                            title = "Tài liệu phổ biến",
                            items = popularDocs,
                            onItemClick = onDocumentClick,
                            onSeeAllClick = { onNavigateToSeeAll("POPULAR") }
                        )
                    }
                    item {
                        DocumentSection(
                            title = "Dành riêng cho bạn",
                            items = recommendedDocs,
                            onItemClick = onDocumentClick,
                            onSeeAllClick = { onNavigateToSeeAll("RECOMMENDED") }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
fun HeaderSection(
    userProfile: User?,
    fallbackName: String,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onLeaderboardClick: () -> Unit // [THÊM MỚI]
) {
    val displayUserName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: fallbackName

    val headerGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF4C9EEB), Color(0xFF1E88E5))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = headerGradient, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .statusBarsPadding()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (!userProfile?.avatarUrl.isNullOrBlank()) {
                    val fullImageUrl = userProfile?.avatarUrl.toFullUrl()
                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Ảnh đại diện của $displayUserName",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = displayUserName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Xin chào 👋", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = displayUserName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ==========================================
            // [THÊM MỚI]: Nút bấm Bảng Xếp Hạng (Cúp vàng)
            // ==========================================
            IconButton(
                onClick = { onLeaderboardClick() },
                modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Text(text = "🏆", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onCommunityClick() },
                modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Forum, contentDescription = "Mở cộng đồng", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onNotificationClick() },
                modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Mở thông báo", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(27.dp), spotColor = Color(0x33000000))
                .background(Color.White, RoundedCornerShape(27.dp))
                .clip(RoundedCornerShape(27.dp))
                .clickable(
                    onClickLabel = "Mở tìm kiếm tài liệu",
                    role = Role.Button,
                    onClick = { onSearchClick() }
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF4C9EEB))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Tìm kiếm tài liệu...", color = Color.Gray, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun DocumentSection(
    title: String,
    items: List<Document>,
    onItemClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E293B))

            Text(
                text = "Xem tất cả",
                color = Color(0xFF4C9EEB),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        onClickLabel = "Xem tất cả tài liệu mục $title",
                        role = Role.Button,
                        onClick = onSeeAllClick
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            EmptyStateView(
                message = "Chưa có tài liệu nào.",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items.size) { index ->
                    DocumentCardPreview(document = items[index], onClick = { onItemClick(items[index].id) })
                }
            }
        }
    }
}

@Composable
fun DocumentCardPreview(document: Document, onClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val cardWidth = screenWidth * 0.4f
    val cardHeight = cardWidth * 1.45f
    val boxImgHeight = cardHeight * 0.55f

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) { }
            .clickable(
                onClickLabel = "Xem chi tiết tài liệu ${document.title}",
                onClick = { onClick() }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxImgHeight)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (!document.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = document.thumbnailUrl.toFullUrl(),
                        contentDescription = "Ảnh bìa của tài liệu ${document.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = document.title.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C9EEB).copy(alpha = 0.5f)
                    )
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))

                document.size?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

// =========================================================================
// COMPOSABLE UI KHUNG XƯƠNG (SKELETON) CHO DANH SÁCH & CARD
// =========================================================================

@Composable
fun SkeletonDocumentSection() {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        // Khung xương tiêu đề danh sách
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .width(160.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Khung xương danh sách cuộn ngang
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false
        ) {
            items(3) {
                SkeletonDocumentCard()
            }
        }
    }
}

@Composable
fun SkeletonDocumentCard() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val cardWidth = screenWidth * 0.4f
    val cardHeight = cardWidth * 1.45f
    val boxImgHeight = cardHeight * 0.55f

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxImgHeight)
                    .shimmerEffect()
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}