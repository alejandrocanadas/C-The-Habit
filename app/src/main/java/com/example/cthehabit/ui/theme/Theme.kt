package com.example.cthehabit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = Cyan,           // CTA principal, botones activos
    onPrimary        = PixelBlack,     // Texto sobre primary
    secondary        = SkyBlue,        // Acento secundario
    onSecondary      = PixelBlack,
    tertiary         = BrightBlue,     // Acento terciario
    onTertiary       = PixelWhite,
    background       = DeepPurple,     // Fondo base de pantallas
    onBackground     = PixelWhite,     // Texto sobre fondo
    surface          = DarkPurple,     // Cards, campos, superficies
    onSurface        = PixelWhite,     // Texto sobre surface
    surfaceVariant   = MidPurple,      // Variante de superficie (inputs, chips)
    onSurfaceVariant = Cyan,           // Texto/iconos sobre surfaceVariant
    outline          = SkyBlue,        // Bordes de inputs y divisores
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