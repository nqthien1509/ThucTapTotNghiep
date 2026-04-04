package com.example.thuctaptotnghiep.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Nút nổi Upload
@Composable
fun UploadButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color(0xFF4C9EEB),
        contentColor = Color.White,
        modifier = modifier.size(60.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(32.dp))
    }
}

// Thanh điều hướng dưới đáy dùng chung cho toàn App
@Composable
fun AppBottomNavigationBar(
    onHomeClick: () -> Unit, // Thêm nút Home
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            containerColor = Color.White,
            contentColor = Color.Gray,
            tonalElevation = 8.dp,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                selected = false, // Tạm thời để false hết để dùng chung nhiều trang
                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF4C9EEB), unselectedIconColor = Color.Gray, indicatorColor = Color.White),
                onClick = onHomeClick // Gắn sự kiện về Home
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                selected = false,
                colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray),
                onClick = onSearchClick
            )

            Spacer(modifier = Modifier.weight(1.5f))

            NavigationBarItem(
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                selected = false,
                colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray),
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                selected = false,
                colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray),
                onClick = onProfileClick
            )
        }

        UploadButton(
            onClick = onUploadClick,
            modifier = Modifier.offset(y = (-30).dp)
        )
    }
}