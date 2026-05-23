package com.example.cthehabit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = Cyan,
    onPrimary        = PixelBlack,
    secondary        = SkyBlue,
    onSecondary      = PixelBlack,
    tertiary         = BrightBlue,
    onTertiary       = PixelWhite,
    background       = DeepPurple,
    onBackground     = PixelWhite,
    surface          = DarkPurple,
    onSurface        = PixelWhite,
    surfaceVariant   = MidPurple,
    onSurfaceVariant = Cyan,
    outline          = SkyBlue,
    error            = Color(0xFFFF6B6B),
    onError          = PixelBlack
)

private val LightColorScheme = lightColorScheme(
    primary          = NavyBlue,
    onPrimary        = PixelWhite,
    secondary        = BrightBlue,
    onSecondary      = PixelWhite,
    tertiary         = SkyBlue,
    onTertiary       = PixelBlack,
    background       = PixelWhite,
    onBackground     = PixelBlack,
    surface          = Color(0xFFF0F4FF),
    onSurface        = PixelBlack,
    surfaceVariant   = Color(0xFFDDE3F5),
    onSurfaceVariant = NavyBlue,
    outline          = MidPurple,
    error            = Color(0xFFB00020),
    onError          = PixelWhite
)

@Composable
fun CTheHabitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}