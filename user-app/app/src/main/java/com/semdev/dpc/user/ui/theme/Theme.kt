package com.semdev.dpc.user.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue800,
    secondary = Blue900,
    tertiary = Green500,
    error = Red500,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun TouchBaseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
