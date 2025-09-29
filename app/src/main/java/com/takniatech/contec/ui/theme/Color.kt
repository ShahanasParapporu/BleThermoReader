package com.takniatech.contec.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
object ContecColors {
    val Primary = Color(0xFF1976D2)
    val PrimaryVariant = Color(0xFF0D47A1)
    val Secondary = Color(0xFF03DAC6)
    val Background = Color(0xFFF5F5F5)
    val Surface = Color.White
    val Error = Color(0xFFB00020)
    val OnPrimary = Color.White
    val OnSurface = Color(0xFF1C1C1E)
    val OnBackground = Color(0xFF1C1C1E)

    @get:Composable
    val AdaptiveText: Color
        get() = if (isSystemInDarkTheme()) Color.White else Color.Black
}