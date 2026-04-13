package com.example.thuctaptotnghiep.ui.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.ui.components.AppBottomNavigationBar
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSearchClick: () -> Unit,
    onUploadClick: () -> Unit,
    onDocumentClick: (String) -> Unit
) {
    val context = LocalContext.current

    val myDocuments by viewModel.myDocuments.collectAsState()
    val favoriteDocuments by viewModel.favoriteDocuments.collectAsState()
    val watchLaterDocuments by viewModel.watchLaterDocuments.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val deleteStatus by viewModel.deleteStatus.collectAsState()

    // STATE: THÔNG TIN USER VÀ DIALOG EDIT
    val userProfile by viewModel.userProfile.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // STATE: BOTTOM SHEET CÀI ĐẶT
    var showBottomSheet by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Tài liệu đã đăng", "Yêu thích", "Xem sau")
    var documentToDelete by remember { mutableStateOf<Document?>(null) }

    val currentList = when (selectedTabIndex) {
        0 -> myDocuments
        1 -> favoriteDocuments
        else -> watchLaterDocuments
    }

    // ==========================================
    // CÔNG CỤ CHỌN ẢNH TỪ ĐIỆN THOẠI
    // ==========================================
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // Ngay khi chọn ảnh xong, gọi hàm upload (Up ngay lập tức)
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
        bottomBar = {
            AppBottomNavigationBar(
                onHomeClick = onBackClick,
                onUploadClick = onUploadClick,
                onProfileClick = { /* Đang ở Profile */ },
                onSearchClick = onSearchClick
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Header lượn sóng
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        lineTo(0f, size.height - 60f)
                        quadraticBezierTo(size.width * 0.4f, size.height + 20f, size.width, size.height - 100f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    drawPath(path, Color(0xFF4C9EEB))
                }

                // NÚT CÀI ĐẶT MỞ BOTTOM SHEET
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp)) {
                    IconButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // KHỐI THÔNG TIN USER VÀ AVATAR
                Row(
                    modifier = Modifier.align(Alignment.TopStart).padding(top = 50.dp, start = 24.dp).fillMaxWidth().padding(end = 60.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayUserName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: viewModel.userName
                    val displaySchool = userProfile?.school?.takeIf { it.isNotBlank() } ?: "Sinh viên UTH"

                    // Khung ảnh ở ngoài màn hình chính (Đã gỡ bỏ hiệu ứng Click)
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile?.avatarUrl.isNullOrBlank()) {
                            val fullImageUrl = "http://10.0.2.2:3000${userProfile!!.avatarUrl}"
                            AsyncImage(
                                model = fullImageUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = displayUserName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = displayUserName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = displaySchool, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))

                        if (!userProfile?.bio.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = userProfile!!.bio!!, fontSize = 12.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-10).dp).background(Color.White, RoundedCornerShape(20.dp)).padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Điểm cống hiến: ${myDocuments.size * 10}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4C9EEB))
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF4C9EEB),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = Color(0xFF4C9EEB), height = 3.dp)
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title, fontSize = 13.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshDocuments() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLoading && !isRefreshing) {
                        CircularProgressIndicator(color = Color(0xFF4C9EEB), modifier = Modifier.align(Alignment.Center))
                    } else if (currentList.isEmpty()) {
                        val emptyMessage = when (selectedTabIndex) {
                            0 -> "Bạn chưa đăng tài liệu nào."
                            1 -> "Bạn chưa yêu thích tài liệu nào."
                            else -> "Danh sách xem sau đang trống."
                        }
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(emptyMessage, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentList) { doc ->
                                ProfileDocumentItem(
                                    document = doc,
                                    isOwner = selectedTabIndex == 0,
                                    onClick = { onDocumentClick(doc.id) },
                                    onDeleteClick = { documentToDelete = doc }
                                )
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
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Cài đặt tài khoản",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.Edit,
                        title = "Chỉnh sửa thông tin",
                        subtitle = "Cập nhật tên, trường học và tiểu sử",
                        iconTint = Color(0xFF4CAF50),
                        onClick = {
                            showBottomSheet = false
                            showEditProfileDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsMenuItem(
                        icon = Icons.Default.Lock,
                        title = "Đổi mật khẩu",
                        subtitle = "Cập nhật mật khẩu để bảo vệ tài khoản",
                        iconTint = Color(0xFF4C9EEB),
                        onClick = {
                            showBottomSheet = false
                            showChangePasswordDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsMenuItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "Đăng xuất",
                        subtitle = "Thoát phiên đăng nhập hiện tại",
                        iconTint = Color.Red,
                        titleColor = Color.Red,
                        onClick = {
                            showBottomSheet = false
                            viewModel.logout { onLogoutClick() }
                        }
                    )
                }
            }
        }

        // =======================================================
        // DIALOG: CHỈNH SỬA THÔNG TIN CÁ NHÂN (CÓ AVATAR Ở ĐÂY)
        // =======================================================
        if (showEditProfileDialog) {
            val fallbackEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

            var editName by remember { mutableStateOf(userProfile?.displayName ?: viewModel.userName) }
            var editSchool by remember { mutableStateOf(userProfile?.school ?: "Trường Đại học Giao thông vận tải TP.HCM (UTH)") }
            var editBio by remember { mutableStateOf(userProfile?.bio ?: "") }
            var isUpdating by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                containerColor = Color.White,
                title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally // Căn giữa nội dung
                    ) {
                        // ---------------------------------------------
                        // KHU VỰC THAY ĐỔI ẢNH ĐẠI DIỆN TRONG DIALOG
                        // ---------------------------------------------
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFFE3F2FD), CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    // Bấm vào đây sẽ mở Album ảnh
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userProfile?.avatarUrl.isNullOrBlank()) {
                                val fullImageUrl = "http://10.0.2.2:3000${userProfile!!.avatarUrl}"
                                AsyncImage(
                                    model = fullImageUrl,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = editName.take(1).uppercase(),
                                    color = Color(0xFF4C9EEB),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Icon Camera nhỏ đè lên góc phải dưới để người dùng dễ nhận biết
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.White, CircleShape)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Đổi ảnh",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Chạm để thay đổi ảnh", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))

                        // ---------------------------------------------
                        // CÁC Ô NHẬP LIỆU
                        // ---------------------------------------------
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Tên hiển thị") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = editSchool,
                            onValueChange = { editSchool = it },
                            label = { Text("Trường học") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Tiểu sử (Bio)") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isUpdating = true
                            viewModel.updateProfile(
                                uid = viewModel.userId,
                                email = userProfile?.email ?: fallbackEmail,
                                name = editName,
                                school = editSchool,
                                bio = editBio,
                                onSuccess = {
                                    isUpdating = false
                                    showEditProfileDialog = false
                                    Toast.makeText(context, "Đã lưu thông tin!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB))
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Lưu", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Hủy", color = Color.Gray)
                    }
                }
            )
        }

        // Dialog Xóa tài liệu
        if (documentToDelete != null) {
            AlertDialog(
                onDismissRequest = { documentToDelete = null },
                title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
                text = { Text("Bạn có chắc muốn xóa '${documentToDelete!!.title}'? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteDocument(documentToDelete!!.id)
                            documentToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Xóa", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { documentToDelete = null }) { Text("Hủy", color = Color.Gray) }
                }
            )
        }

        // Dialog Đổi mật khẩu
        if (showChangePasswordDialog) {
            var newPassword by remember { mutableStateOf("") }
            var isProcessing by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showChangePasswordDialog = false },
                containerColor = Color.White,
                title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mật khẩu mới (Tối thiểu 6 ký tự)") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isProcessing = true
                            viewModel.changePassword(
                                newPassword = newPassword,
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
                        enabled = newPassword.length >= 6 && !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB))
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Xác nhận", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePasswordDialog = false }) {
                        Text("Hủy", color = Color.Gray)
                    }
                }
            )
        }
    }
}

// ==========================================
// COMPONENT CON: Hàng Menu trong Bottom Sheet
// ==========================================
@Composable
fun SettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    titleColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconTint.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 13.sp, color = Color.Gray)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}

@Composable
fun ProfileDocumentItem(
    document: Document,
    isOwner: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(56.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(text = document.title.take(1).uppercase(), fontWeight = FontWeight.ExtraBold, color = Color(0xFF4C9EEB), fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = document.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    val (statusText, statusColor) = when (document.status) {
                        "verified" -> "✓ Đã kiểm duyệt" to Color(0xFF4CAF50)
                        "failed" -> "⚠ Lỗi xử lý" to Color(0xFFF44336)
                        else -> "● Đang xử lý..." to Color(0xFFFF9800)
                    }

                    Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = document.size ?: "N/A", fontSize = 11.sp, color = Color.Gray)
                }

                Text(text = "Ngày đăng: ${document.uploadDate.take(10)}", fontSize = 10.sp, color = Color.LightGray)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Text(text = "${document.downloads}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            if (isOwner) {
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFFCDD2), modifier = Modifier.size(20.dp))
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}