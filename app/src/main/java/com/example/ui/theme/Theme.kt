package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NaturalGreenPrimary,
    secondary = NaturalGreenSecondary,
    tertiary = NaturalGreenTertiary,
    background = NaturalBgDark,
    surface = NaturalSurfaceDark,
    surfaceVariant = NaturalSurfaceVariantDark,
    onBackground = NaturalOnBgDark,
    onSurface = NaturalOnBgDark,
    primaryContainer = NaturalGreenPrimary.copy(alpha = 0.35f),
    secondaryContainer = NaturalGreenSecondary.copy(alpha = 0.35f)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalGreenPrimary,
    secondary = NaturalGreenSecondary,
    tertiary = NaturalGreenTertiary,
    background = NaturalBgLight,
    surface = NaturalSurfaceLight,
    surfaceVariant = NaturalSurfaceVariantLight,
    onBackground = NaturalOnBgLight,
    onSurface = NaturalOnBgLight,
    onSurfaceVariant = NaturalGreenSecondary,
    primaryContainer = NaturalHeroContainer,
    onPrimaryContainer = NaturalOnHeroContainer,
    secondaryContainer = NaturalPillActive,
    onSecondaryContainer = NaturalGreenPrimary,
    outline = NaturalBorderLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color so that the requested "Natural Tones" brand identity is strictly preserved.
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
