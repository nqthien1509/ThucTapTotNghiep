package com.example.thuctaptotnghiep.ui.home

import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.example.thuctaptotnghiep.utils.UserManager
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
// <-- IMPORT MỚI: Dành cho các Component trạng thái dùng chung
import com.example.thuctaptotnghiep.ui.components.EmptyStateView
import com.example.thuctaptotnghiep.ui.components.LoadingStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToUpload: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val documentList by viewModel.documents.collectAsState()
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
                onHomeClick = { /* Đang ở Home */ },
                onUploadClick = onNavigateToUpload,
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick
            )
        },
        containerColor = Color(0xFFF5F5F5)
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
                        onSearchClick = onSearchClick
                    )
                }

                if (isLoading && !isRefreshing) {
                    item {
                        // CẢI TIẾN: Sử dụng LoadingStateView dùng chung, giới hạn chiều cao 30% màn hình
                        LoadingStateView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillParentMaxHeight(0.3f)
                        )
                    }
                } else {
                    item { DocumentSection(title = "Mới được tải lên", items = documentList, onItemClick = onDocumentClick) }
                    item { DocumentSection(title = "Tài liệu ôn thi", items = documentList.shuffled(), onItemClick = onDocumentClick) }
                    item { DocumentSection(title = "Tin nổi bật", items = documentList, onItemClick = onDocumentClick) }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
fun HeaderSection(userProfile: User?, fallbackName: String, onSearchClick: () -> Unit) {
    val displayUserName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: fallbackName

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF4C9EEB), shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (!userProfile?.avatarUrl.isNullOrBlank()) {
                    val fullImageUrl = "http://10.0.2.2:3000${userProfile!!.avatarUrl}"
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Xin chào,", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text(displayUserName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .clickable(
                    onClickLabel = "Mở tìm kiếm tài liệu",
                    role = Role.Button,
                    onClick = { onSearchClick() }
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Tìm kiếm tài liệu...", color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun DocumentSection(title: String, items: List<Document>, onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            Text(
                text = "Xem tất cả",
                color = Color(0xFF4C9EEB),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        onClickLabel = "Xem tất cả tài liệu mục $title",
                        role = Role.Button,
                        onClick = { /* Todo: Xử lý click chuyển trang */ }
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        if (items.isEmpty()) {
            // CẢI TIẾN: Tận dụng EmptyStateView dùng chung, giới hạn chiều cao để không bị chiếm full màn
            EmptyStateView(
                message = "Chưa có tài liệu nào.",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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

    val cardWidth = screenWidth * 0.38f
    val cardHeight = cardWidth * 1.4f
    val boxImgHeight = cardHeight * 0.55f

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .semantics(mergeDescendants = true) { }
            .clickable(
                onClickLabel = "Xem chi tiết tài liệu ${document.title}",
                onClick = { onClick() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxImgHeight)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = document.title.take(1).uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C9EEB)
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))

                document.size?.let {
                    Text(text = it, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}