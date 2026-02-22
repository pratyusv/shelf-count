package com.shelfcount.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = MidnightPrimary,
        onPrimary = OnMidnightPrimary,
        primaryContainer = MidnightContainer,
        onPrimaryContainer = OnMidnightContainer,
        secondary = SlateSecondary,
        onSecondary = OnSlateSecondary,
        secondaryContainer = SlateContainer,
        onSecondaryContainer = OnSlateContainer,
        tertiary = CoralTertiary,
        onTertiary = OnCoralTertiary,
        tertiaryContainer = CoralContainer,
        onTertiaryContainer = OnCoralContainer,
        background = AppBackground,
        surface = AppSurface,
        surfaceVariant = AppSurfaceVariant,
        outline = AppOutline,
        error = AppError,
        onError = OnAppError,
        errorContainer = AppErrorContainer,
        onErrorContainer = OnAppErrorContainer,
    )

private val DarkColors =
    darkColorScheme(
        primary = MidnightContainer,
        onPrimary = OnMidnightContainer,
        secondary = SlateContainer,
        onSecondary = OnSlateContainer,
        tertiary = CoralContainer,
        onTertiary = OnCoralContainer,
    )

@Composable
fun ShelfCountTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
