package com.example.thuctaptotnghiep.ui.search

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications // [THÊM]: Import icon thông báo
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import androidx.compose.foundation.layout.statusBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit, // [THÊM]: Tham số điều hướng sang thông báo
    initialCategory: String? = null,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val categories by viewModel.categories.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    LaunchedEffect(initialCategory) {
        if (!initialCategory.isNullOrBlank()) {
            viewModel.onCategorySelected(initialCategory)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, "Lỗi tìm kiếm: $it", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                currentRoute = "search",
                onHomeClick = onHomeClick,
                onUploadClick = onUploadClick,
                onProfileClick = onProfileClick,
                onSearchClick = { }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ==========================================
            // HEADER TÌM KIẾM LIỀN MẠCH (Gradient & Shadow)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), spotColor = Color(0x26000000))
                    .background(
                        brush = Brush.verticalGradient(listOf(Color(0xFF4C9EEB), Color(0xFF1E88E5))),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .statusBarsPadding()
            ) {
                // [CẬP NHẬT UI]: Bố cục lại Header để thêm nút Chuông thông báo
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text("Khám phá tài liệu", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }

                    // Nút chuông thông báo
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .clip(CircleShape)
                            .clickable { onNotificationClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Thanh Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    placeholder = { Text("Bạn đang tìm môn học gì?", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF4C9EEB)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.onSearchQueryChange("")
                                keyboardController?.show()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(50),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            viewModel.onSearchAction(searchQuery)
                        }
                    )
                )

                // Thanh Filter
                if (categories.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.onCategorySelected(category)
                                    keyboardController?.hide()
                                },
                                label = {
                                    Text(
                                        text = category,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = Color(0xFF1E88E5),
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    labelColor = Color.White
                                ),
                                border = null,
                                shape = RoundedCornerShape(20.dp),
                                modifier = if (isSelected) Modifier.shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33000000)) else Modifier
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // ==========================================
            // KHU VỰC HIỂN THỊ KẾT QUẢ / LỊCH SỬ
            // ==========================================
            Box(modifier = Modifier.fillMaxSize()) {

                if (isSearching) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4C9EEB), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đang tìm kiếm kho tài liệu...", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
                else if (searchQuery.isBlank() && !hasSearched && recentSearches.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp)) {
                        Text("Tìm kiếm gần đây", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 16.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recentSearches) { recentItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .clickable {
                                            keyboardController?.hide()
                                            viewModel.onSearchAction(recentItem)
                                        }
                                        .padding(vertical = 14.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.History, contentDescription = null, tint = Color(0xFF94A3B8))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = recentItem, fontSize = 15.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
                else if (hasSearched && searchResults.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Không tìm thấy kết quả nào", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Thử sử dụng các từ khóa chung chung hơn hoặc chọn bộ lọc khác xem sao nhé.", textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp)
                    }
                }
                else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (hasSearched) {
                            item {
                                Text("Tìm thấy ${searchResults.size} kết quả", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 8.dp))
                            }
                        }
                        items(searchResults, key = { it.id }) { doc ->
                            SearchResultItem(document = doc, onClick = { onDocumentClick(doc.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(document: Document, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = document.title.take(1).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4C9EEB),
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Đăng bởi: ${document.authorName}", color = Color(0xFF64748B), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))

                    val category = if (document.category.isNullOrBlank()) "Tài liệu" else document.category!!
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = category,
                            color = Color(0xFF059669),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                document.size?.let { Text(text = it, color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Medium) }
            }
        }
    }
}