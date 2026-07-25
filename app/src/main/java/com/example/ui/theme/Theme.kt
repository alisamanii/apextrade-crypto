package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DarkObsidian,
    primaryContainer = BullishBg,
    onPrimaryContainer = NeonGreen,
    secondary = AmberGold,
    onSecondary = DarkObsidian,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = CrimsonRed,
    onTertiary = TextPrimary,
    tertiaryContainer = BearishBg,
    onTertiaryContainer = CrimsonRed,
    background = DarkObsidian,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder,
    outlineVariant = SlateBorder
)

@Composable
fun ApexTradeTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkObsidian.toArgb()
            window.navigationBarColor = DarkObsidian.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
