package com.example.thuctaptotnghiep.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.utils.toFullUrl // [QUAN TRỌNG]: Import hàm nối URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocumentDetail: (String) -> Unit, // Bấm vào tài liệu thì mở chi tiết
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val topDocuments by viewModel.topDocuments.collectAsState()
    val topContributors by viewModel.topContributors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Quản lý Tab được chọn (0: Tài liệu, 1: Người dùng)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("🔥 Tài liệu Hot", "🏆 Top Đóng góp")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bảng xếp hạng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchLeaderboards() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Thanh Tabs chuyển đổi
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Hiển thị vòng xoay loading
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Nội dung danh sách theo Tab
                if (selectedTabIndex == 0) {
                    TopDocumentsList(
                        documents = topDocuments,
                        onItemClick = onNavigateToDocumentDetail
                    )
                } else {
                    TopContributorsList(users = topContributors)
                }
            }
        }
    }
}

@Composable
fun TopDocumentsList(documents: List<Document>, onItemClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(documents) { index, doc ->
            Card(
                onClick = { onItemClick(doc.id) }, // [ĐÃ FIX]: Dùng id thay vì _id
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Số thứ hạng (1, 2, 3...)
                    RankBadge(rank = index + 1)

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = doc.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "Đăng bởi: ${doc.authorName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(text = "💾 ${doc.downloads} lượt tải  |  👀 ${doc.views} lượt xem", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun TopContributorsList(users: List<User>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(users) { index, user ->
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Số thứ hạng
                    RankBadge(rank = index + 1)

                    Spacer(modifier = Modifier.width(12.dp))

                    // Avatar người dùng
                    AsyncImage(
                        // [ĐÃ FIX]: Check an toàn biến Nullable và dùng toFullUrl()
                        model = if (!user.avatarUrl.isNullOrEmpty()) user.avatarUrl.toFullUrl() else "https://via.placeholder.com/150",
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // [ĐÃ FIX]: Check an toàn biến displayName có thể Null
                        Text(text = if (!user.displayName.isNullOrEmpty()) user.displayName else "Người dùng ẩn danh", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = user.level ?: "Tân binh", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFA500)) // Màu cam cho danh hiệu
                        Text(text = "📤 Đã đóng góp: ${user.totalUploads} tài liệu", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    }

                    // Điểm uy tín
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "${user.reputationScore}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Điểm", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Composable vẽ cái hình tròn hiển thị hạng (Màu vàng, bạc, đồng cho top 3)
@Composable
fun RankBadge(rank: Int) {
    val backgroundColor = when (rank) {
        1 -> Color(0xFFFFD700) // Vàng
        2 -> Color(0xFFC0C0C0) // Bạc
        3 -> Color(0xFFCD7F32) // Đồng
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            color = if (rank <= 3) Color.White else Color.DarkGray,
            fontWeight = FontWeight.Bold
        )
    }
}