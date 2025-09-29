package com.takniatech.contec.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


val ContecShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)
private val LightColorScheme = lightColorScheme(
    primary = ContecColors.Primary,
    primaryContainer = ContecColors.PrimaryVariant,
    secondary = ContecColors.Secondary,
    background = ContecColors.Background,
    surface = ContecColors.Surface,
    error = ContecColors.Error,
    onPrimary = ContecColors.OnPrimary,
    onSecondary = ContecColors.OnPrimary,
    onBackground = ContecColors.OnBackground,
    onSurface = ContecColors.OnSurface,
    onError = ContecColors.OnPrimary
)
private val DarkColorScheme = darkColorScheme(
    primary = ContecColors.Primary,
    primaryContainer = ContecColors.PrimaryVariant,
    secondary = ContecColors.Secondary,
    background = ContecColors.OnSurface,   // slightly dark background for dark mode
    surface = ContecColors.OnSurface,
    error = ContecColors.Error,
    onPrimary = ContecColors.OnPrimary,
    onSecondary = ContecColors.OnPrimary,
    onBackground = ContecColors.OnBackground,
    onSurface = ContecColors.OnSurface,
    onError = ContecColors.OnPrimary
)

@Composable
fun ContecTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = ContecTypography,
        shapes = ContecShapes,
        content = content
    )
}