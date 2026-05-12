package com.example.thuctaptotnghiep.ui.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.thuctaptotnghiep.ui.components.EmptyStateView
import com.example.thuctaptotnghiep.ui.components.LoadingStateView
import com.example.thuctaptotnghiep.utils.toFullUrl
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSearchClick: () -> Unit,
    onUploadClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val myDocuments by viewModel.myDocuments.collectAsState()
    val favoriteDocuments by viewModel.favoriteDocuments.collectAsState()
    val watchLaterDocuments by viewModel.watchLaterDocuments.collectAsState()
    val pendingDeleteIds by viewModel.pendingDeleteIds.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val deleteStatus by viewModel.deleteStatus.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Đã đăng", "Yêu thích", "Xem sau")

    val currentList = when (selectedTabIndex) {
        0 -> myDocuments
        1 -> favoriteDocuments
        else -> watchLaterDocuments
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.uploadAvatar(context, viewModel.userId, it) }
    }

    LaunchedEffect(errorMessage, deleteStatus) {
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        deleteStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetDeleteStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavigationBar(
                onHomeClick = onBackClick,
                onUploadClick = onUploadClick,
                onProfileClick = { },
                onSearchClick = onSearchClick
            )
        },
        containerColor = Color(0xFFF8F9FA) // Nền sáng thanh lịch
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            val headerHeight = screenHeight * 0.28f

            // [CẬP NHẬT UI]: Header Gradient hiện đại, bo góc sâu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight + 20.dp) // Tăng height chút để bù cho phần vát góc
            ) {
                // Background Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                        .background(
                            brush = Brush.verticalGradient(listOf(Color(0xFF4C9EEB), Color(0xFF1E88E5))),
                            shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                        )
                        .shadow(4.dp, RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                )

                // Nút cài đặt góc trên phải
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 12.dp, end = 16.dp)
                ) {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Cài đặt", tint = Color.White)
                        }
                    }
                }

                // Thông tin User
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(top = 24.dp, start = 24.dp, end = 64.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayUserName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: viewModel.userName
                    val displaySchool = userProfile?.school?.takeIf { it.isNotBlank() } ?: "Sinh viên UTH"

                    // [CẬP NHẬT UI]: Avatar có viền trắng và bóng mờ
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(8.dp, CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .background(Color.White, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = userProfile?.avatarUrl?.toFullUrl(),
                                contentDescription = "Ảnh đại diện",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                                Text(
                                    text = displayUserName.take(1).uppercase(),
                                    color = Color(0xFF4C9EEB),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = displayUserName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = displaySchool, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)

                        if (!userProfile?.bio.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = userProfile!!.bio!!, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // [CẬP NHẬT UI]: Thẻ nổi lơ lửng hiển thị điểm cống hiến
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 40.dp)
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0x26000000)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).background(Color(0xFFFFF8E1), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Điểm cống hiến", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                            Text("${myDocuments.size * 10} Pt", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TabRow thanh thoát hơn
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF4C9EEB),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF4C9EEB),
                        height = 3.dp
                    )
                },
                divider = {} // Xoá gạch chân mờ mặc định
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) Color(0xFF1E293B) else Color.Gray
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshDocuments() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        isLoading && !isRefreshing -> {
                            LoadingStateView()
                        }
                        currentList.filter { !pendingDeleteIds.contains(it.id) }.isEmpty() -> {
                            val emptyMessage = when (selectedTabIndex) {
                                0 -> "Bạn chưa đăng tài liệu nào."
                                1 -> "Bạn chưa yêu thích tài liệu nào."
                                else -> "Danh sách xem sau đang trống."
                            }
                            EmptyStateView(
                                message = emptyMessage,
                                icon = if (selectedTabIndex == 1) Icons.Default.FavoriteBorder else Icons.Default.Inbox
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val visibleDocs = currentList.filter { !pendingDeleteIds.contains(it.id) }

                                items(visibleDocs, key = { it.id }) { doc ->
                                    ProfileDocumentItem(
                                        document = doc,
                                        isOwner = selectedTabIndex == 0,
                                        onClick = { onDocumentClick(doc.id) },
                                        onDeleteClick = {
                                            viewModel.deleteDocumentWithUndo(doc.id)
                                            coroutineScope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Đã xóa tài liệu",
                                                    actionLabel = "HOÀN TÁC",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.undoDelete(doc.id)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODAL BOTTOM SHEET (CÀI ĐẶT)
        // ==========================================
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text("Cài đặt tài khoản", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 24.dp))

                    SettingsMenuItem(
                        icon = Icons.Default.Edit, title = "Chỉnh sửa thông tin", subtitle = "Cập nhật tên, trường học và tiểu sử", iconTint = Color(0xFF10B981),
                        onClick = { showBottomSheet = false; showEditProfileDialog = true }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsMenuItem(
                        icon = Icons.Default.Lock, title = "Đổi mật khẩu", subtitle = "Cập nhật mật khẩu để bảo vệ tài khoản", iconTint = Color(0xFF4C9EEB),
                        onClick = { showBottomSheet = false; showChangePasswordDialog = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsMenuItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp, title = "Đăng xuất", subtitle = "Thoát phiên đăng nhập hiện tại", iconTint = Color(0xFFEF4444), titleColor = Color(0xFFEF4444),
                        onClick = { showBottomSheet = false; viewModel.logout { onLogoutClick() } }
                    )
                }
            }
        }

        // ==========================================
        // DIALOG: CHỈNH SỬA THÔNG TIN CÁ NHÂN
        // ==========================================
        if (showEditProfileDialog) {
            val fallbackEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
            var editName by remember { mutableStateOf(userProfile?.displayName ?: viewModel.userName) }
            var editSchool by remember { mutableStateOf(userProfile?.school ?: "Trường Đại học Giao thông vận tải TP.HCM (UTH)") }
            var editBio by remember { mutableStateOf(userProfile?.bio ?: "") }
            var isUpdating by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E293B)) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                                .clip(CircleShape)
                                .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userProfile?.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = userProfile?.avatarUrl?.toFullUrl(),
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(text = editName.take(1).uppercase(), color = Color(0xFF94A3B8), fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            }
                            // Icon Camera phủ đè
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(28.dp)
                                    .background(Color(0xFF4C9EEB), CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Tên hiển thị") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = editSchool, onValueChange = { editSchool = it }, label = { Text("Trường học") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("Tiểu sử (Bio)") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), maxLines = 3)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isUpdating = true
                            viewModel.updateProfile(
                                uid = viewModel.userId, email = userProfile?.email ?: fallbackEmail, name = editName, school = editSchool, bio = editBio,
                                onSuccess = { isUpdating = false; showEditProfileDialog = false; Toast.makeText(context, "Đã lưu thông tin!", Toast.LENGTH_SHORT).show() }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("Lưu thay đổi", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) { Text("Hủy", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            )
        }

        // ==========================================
        // DIALOG: ĐỔI MẬT KHẨU BẢO MẬT
        // ==========================================
        if (showChangePasswordDialog) {
            var currentPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var isProcessing by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showChangePasswordDialog = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Đổi mật khẩu", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E293B)) },
                text = {
                    Column {
                        Text("Vì lý do bảo mật, vui lòng nhập mật khẩu hiện tại.", fontSize = 14.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Mật khẩu hiện tại") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Mật khẩu mới (Từ 6 ký tự)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isProcessing = true
                            viewModel.changePassword(
                                currentPass = currentPassword,
                                newPass = newPassword,
                                onSuccess = {
                                    isProcessing = false
                                    showChangePasswordDialog = false
                                    Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { errorMsg ->
                                    isProcessing = false
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = currentPassword.isNotEmpty() && newPassword.length >= 6 && !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("Xác nhận", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePasswordDialog = false }) { Text("Hủy", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            )
        }
    }
}

// ==========================================
// COMPONENT CON
// ==========================================
@Composable
fun SettingsMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, iconTint: Color, titleColor: Color = Color(0xFF1E293B), onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() }.padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp).background(iconTint.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 13.sp, color = Color(0xFF64748B))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFCBD5E1))
    }
}

@Composable
fun ProfileDocumentItem(document: Document, isOwner: Boolean, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    // [CẬP NHẬT UI]: Card mềm mại, đổ bóng 3D
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Icon tài liệu
            Box(
                modifier = Modifier.size(60.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = document.title.take(1).uppercase(), fontWeight = FontWeight.ExtraBold, color = Color(0xFF4C9EEB), fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = document.title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Trạng thái hiển thị dạng Chip
                    val (statusText, statusColor, statusBg) = when (document.status) {
                        "verified" -> Triple("Đã duyệt", Color(0xFF059669), Color(0xFFD1FAE5))
                        "failed" -> Triple("Lỗi", Color(0xFFDC2626), Color(0xFFFEE2E2))
                        else -> Triple("Đang xử lý", Color(0xFFD97706), Color(0xFFFEF3C7))
                    }

                    Surface(color = statusBg, shape = RoundedCornerShape(6.dp)) {
                        Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = document.size ?: "N/A", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Đăng ngày: ${document.uploadDate.take(10)}", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 8.dp)) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "${document.downloads}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            }

            if (isOwner) {
                // Nút xóa được thiết kế lại an toàn và đẹp hơn
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp).background(Color(0xFFFEE2E2), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}