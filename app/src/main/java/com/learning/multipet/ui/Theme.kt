package com.learning.multipet.ui


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val ScreenBg = Color(0xFFF2F7F7)
    val Teal = Color(0xFF0A8E9A)
    val TealDark = Color(0xFF087884)
    val Orange = Color(0xFFFF8A3D)
    val TextMuted = Color(0xFF6C8A92)
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppColors.Teal,
            secondary = AppColors.Orange,
            background = AppColors.ScreenBg,
            surface = Color.White,
            onPrimary = Color.White
        ),
        content = content
    )
}
