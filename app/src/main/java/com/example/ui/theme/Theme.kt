package com.example.ui.theme

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

private val PicassoLightScheme = lightColorScheme(
    primary = PicassoBlue,
    onPrimary = Color.White,
    primaryContainer = PicassoBlueSoft,
    onPrimaryContainer = PicassoBlue,
    secondary = PicassoTerracotta,
    onSecondary = Color.White,
    secondaryContainer = PicassoCanvasWarm,
    onSecondaryContainer = PicassoInk,
    tertiary = PicassoOchre,
    onTertiary = PicassoInk,
    background = PicassoCanvas,
    onBackground = PicassoInk,
    surface = PicassoCanvasWarm,
    onSurface = PicassoInk,
    surfaceVariant = Color(0xFFE5DDD0),
    onSurfaceVariant = PicassoInkMuted,
    error = PicassoVermilion,
    onError = Color.White,
    errorContainer = PicassoVermilionWash,
    onErrorContainer = PicassoVermilionDark
)

private val PicassoDarkScheme = darkColorScheme(
    primary = Color(0xFF6B9DE8),
    onPrimary = Color(0xFF0F1E33),
    primaryContainer = PicassoDarkSurfaceVariant,
    onPrimaryContainer = Color(0xFFD6E3F0),
    secondary = PicassoTerracotta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2C2A28),
    onSecondaryContainer = PicassoDarkInk,
    tertiary = PicassoOchre,
    onTertiary = Color.Black,
    background = PicassoDarkBackground,
    onBackground = PicassoDarkInk,
    surface = PicassoDarkSurface,
    onSurface = PicassoDarkInk,
    surfaceVariant = PicassoDarkSurfaceVariant,
    onSurfaceVariant = PicassoDarkInkWash,
    error = PicassoVermilion,
    onError = Color.White,
    errorContainer = Color(0xFF4A1410),
    onErrorContainer = Color(0xFFFFB4AB)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep authentic Picasso palette intact
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) PicassoDarkScheme else PicassoLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
