package com.cinemax.peru.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CineRojo,
    onPrimary = Color.White,
    secondary = CineOro,
    onSecondary = CineOscuro,
    background = CineOscuro,
    onBackground = CineOnOscuro,
    surface = CineGrisOscuro,
    onSurface = CineOnOscuro,
    error = Color(0xFFFF5252)
)

private val LightColorScheme = lightColorScheme(
    primary = CineRojo,
    onPrimary = Color.White,
    secondary = Color(0xFF7A5400),
    onSecondary = Color.White,
    background = CineClaro,
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB3261E)
)

@Composable
fun CineMaxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}