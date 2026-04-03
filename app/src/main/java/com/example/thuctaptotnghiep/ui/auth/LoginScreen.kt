package com.example.thuctaptotnghiep.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val loginState by viewModel.loginState.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var rememberPassword by remember { mutableStateOf(false) } // Checkbox Nhớ mật khẩu
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
            is LoginState.Error -> {
                Toast.makeText(context, (loginState as LoginState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // Box ngoài cùng chứa các hình vẽ lượn sóng và nội dung
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // Vẽ hình lượn sóng Header (Phía trên)
        Canvas(modifier = Modifier.fillMaxWidth().height(250.dp).align(Alignment.TopCenter)) {
            val path = Path().apply {
                lineTo(0f, size.height * 0.7f)
                quadraticBezierTo(size.width * 0.5f, size.height * 1.1f, size.width, size.height * 0.5f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, Color(0xFF4C9EEB))
        }

        // Vẽ hình lượn sóng Footer (Phía dưới)
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp).align(Alignment.BottomCenter)) {
            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, size.height * 0.6f)
                quadraticBezierTo(size.width * 0.5f, 0f, size.width, size.height * 0.3f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, Color(0xFF4C9EEB))
        }

        // Nội dung chính có thể cuộn được (Tránh bị bàn phím che)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp)) // Đẩy nội dung xuống

            // Avatar tròn
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(80.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Đăng Nhập", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(32.dp))

            // Khung nhập Email bo tròn
            TextField(
                value = email,
                onValueChange = { viewModel.updateEmail(it) },
                placeholder = { Text("Email", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F3F3),
                    unfocusedContainerColor = Color(0xFFF3F3F3),
                    focusedIndicatorColor = Color.Transparent, // Tắt đường gạch dưới
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Khung nhập Mật khẩu bo tròn
            TextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                placeholder = { Text("Password", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = null, tint = Color.Gray)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F3F3),
                    unfocusedContainerColor = Color(0xFFF3F3F3),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Dòng Nhớ mật khẩu & Quên mật khẩu
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberPassword,
                        onCheckedChange = { rememberPassword = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4C9EEB))
                    )
                    Text("Nhớ mật khẩu", fontSize = 14.sp)
                }
                Text(
                    text = "Quên mật khẩu?",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.clickable { /* Xử lý quên MK */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Đăng nhập bo tròn
            Button(
                onClick = { viewModel.login() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB)),
                shape = RoundedCornerShape(16.dp),
                enabled = loginState != LoginState.Loading
            ) {
                if (loginState == LoginState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Đăng Nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chữ "hoặc" có đường kẻ
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(0.8f)) {
                Divider(modifier = Modifier.weight(1f), color = Color.Gray)
                Text(" hoặc ", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray)
                Divider(modifier = Modifier.weight(1f), color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Các nút Mạng xã hội (Dùng các vòng tròn màu giả lập tạm thời)
            // Sau này bạn có thể tải icon PNG từ Figma cho vào thư mục res/drawable và dùng lệnh Image()
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SocialIconPlaceholder(color = Color(0xFF3B5998)) // Facebook Blue
                SocialIconPlaceholder(color = Color(0xFFDB4437)) // Google Red
                SocialIconPlaceholder(color = Color(0xFF00A4EF)) // Microsoft Blue
                SocialIconPlaceholder(color = Color(0xFF171515)) // Github Black
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Dòng chữ "Chưa có tài khoản..." đè lên Footer
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Chưa có tài khoản, ", color = Color.DarkGray)
            Text(
                text = "Đăng kí",
                color = Color.White, // Đổi màu trắng cho nổi trên nền xanh
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

// Widget vẽ hình tròn giả lập Icon MXH
@Composable
fun SocialIconPlaceholder(color: Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        // Tạm để trống, sau này thay bằng Icon thực tế
    }
}