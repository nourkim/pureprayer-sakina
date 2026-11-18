package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.model.SakinaThemeId

val LocalSakinaPalette = staticCompositionLocalOf { SakinaPalettes.RawdahEmerald }

@Composable
fun SakinaTheme(
    themeId: SakinaThemeId,
    isArabic: Boolean,
    content: @Composable () -> Unit
) {
    val palette = SakinaPalettes.forId(themeId)

    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            secondary = palette.accent,
            background = palette.gradientTop,
            surface = palette.surfaceElevated,
            surfaceVariant = palette.surfaceCard,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.borderSubtle
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            secondary = palette.accent,
            background = palette.gradientTop,
            surface = palette.surfaceElevated,
            surfaceVariant = palette.surfaceCard,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.borderSubtle
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !palette.isDark
                controller.isAppearanceLightNavigationBars = !palette.isDark
            }
        }
    }

    CompositionLocalProvider(LocalSakinaPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = sakinaTypography(isArabic),
            content = content
        )
    }
}
