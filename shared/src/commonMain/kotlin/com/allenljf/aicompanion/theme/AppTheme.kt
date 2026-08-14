package com.allenljf.aicompanion.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// demo 只做 light theme；colorScheme 由 Tokens 映射，非官方 KK 色階，僅取合理對應。
private val LightColors = lightColorScheme(
    primary = Tokens.colorBackgroundPrimaryButton,
    onPrimary = Tokens.colorWhite,
    primaryContainer = Tokens.colorBackgroundPrimaryLight,
    onPrimaryContainer = Tokens.colorTextPrimaryDark,
    background = Tokens.colorWhite,
    onBackground = Tokens.colorTextDarker,
    surface = Tokens.colorBackgroundSurfaceLight,
    onSurface = Tokens.colorTextDark,
    surfaceVariant = Tokens.colorBackgroundSurfaceMedium,
    onSurfaceVariant = Tokens.colorTextMedium,
    error = Tokens.colorBackgroundCriticalMedium,
    onError = Tokens.colorWhite,
    outline = Tokens.colorBorderLight,
    outlineVariant = Tokens.colorBorderLighter,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
