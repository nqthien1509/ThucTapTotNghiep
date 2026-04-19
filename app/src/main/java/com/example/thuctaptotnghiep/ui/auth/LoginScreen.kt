package com.example.thuctaptotnghiep.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(), // Sử dụng hiltViewModel ở đây
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val authMessage by viewModel.authMessage.collectAsState()
    val isAuthSuccess by viewModel.isAuthSuccess.collectAsState()

    LaunchedEffect(isAuthSuccess) {
        if (isAuthSuccess) onLoginSuccess()
    }

    LaunchedEffect(authMessage) {
        authMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetMessage()
        }
    }

    // Thay thế Box bằng Column và thêm các Modifier xử lý Insets/Scroll
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
            .safeDrawingPadding() // Tránh đè lên status bar & navigation bar
            .verticalScroll(rememberScrollState()) // Cho phép cuộn khi bàn phím bật
            .imePadding(), // Đẩy nội dung lên khi bàn phím xuất hiện
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Giữ Card ở giữa màn hình
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 24.dp), // Thêm khoảng cách an toàn khi cuộn
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ĐĂNG NHẬP",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C9EEB)
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.login(email.trim(), password.trim()) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Chưa có tài khoản? ", color = Color.Gray)
                    Text(
                        text = "Đăng ký ngay",
                        color = Color(0xFF4C9EEB),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}