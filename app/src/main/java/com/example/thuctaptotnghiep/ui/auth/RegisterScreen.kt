package com.example.thuctaptotnghiep.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isTermsAccepted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val authMessage by viewModel.authMessage.collectAsState()
    // ĐÃ XÓA DÒNG: val isAuthSuccess by viewModel.isAuthSuccess.collectAsState()

    val nameError by viewModel.nameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()

    // [CẬP NHẬT LUỒNG LẮNG NGHE LỖI]: Bắt sự kiện chuyển trang 1 lần duy nhất
    LaunchedEffect(Unit) {
        viewModel.authSuccessEvent.collect {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(authMessage) {
        authMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetMessage()
        }
    }

    LaunchedEffect(authMessage) {
        authMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetMessage()
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF4C9EEB), Color(0xFF1E88E5))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tạo tài khoản mới",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "Cùng tham gia cộng đồng chia sẻ tri thức",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color(0x40000000))
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val textFieldColors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    errorContainerColor = Color(0xFFFEF2F2),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF1E293B),
                    unfocusedTextColor = Color(0xFF1E293B)
                )

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Họ và Tên", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF4C9EEB)) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = { if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = Color(0xFF4C9EEB)) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = { if (emailError != null) Text(emailError!!, color = MaterialTheme.colorScheme.error) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Mật khẩu", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF4C9EEB)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Hiện/Ẩn mật khẩu", tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = { if (passwordError != null) Text(passwordError!!, color = MaterialTheme.colorScheme.error) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Xác nhận mật khẩu", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF4C9EEB)) },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = "Hiện/Ẩn xác nhận", tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    singleLine = true,
                    isError = confirmPasswordError != null,
                    supportingText = { if (confirmPasswordError != null) Text(confirmPasswordError!!, color = MaterialTheme.colorScheme.error) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isTermsAccepted,
                        onCheckedChange = { isTermsAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4C9EEB))
                    )
                    Text(
                        text = "Tôi đồng ý với Điều khoản và Chính sách",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.clickable { isTermsAccepted = !isTermsAccepted }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.register(name.trim(), email.trim(), password.trim(), confirmPassword.trim(), isTermsAccepted)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color(0x404C9EEB)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C9EEB)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("ĐĂNG KÝ TÀI KHOẢN", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Đã có tài khoản? ", color = Color(0xFF64748B), fontSize = 14.sp)
                    Text(
                        text = "Đăng nhập",
                        color = Color(0xFF4C9EEB),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}