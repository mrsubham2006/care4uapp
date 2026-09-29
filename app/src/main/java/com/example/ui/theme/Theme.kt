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

private val DarkColorScheme = darkColorScheme(
  primary = TealAccent,
  onPrimary = BlueDeep,
  primaryContainer = TealDark,
  onPrimaryContainer = TealLight,
  secondary = CyanAccent,
  onSecondary = BlueDeep,
  secondaryContainer = Slate800,
  onSecondaryContainer = Slate100,
  tertiary = WarningAmber,
  onTertiary = Slate900,
  background = NavyDark,
  onBackground = Slate100,
  surface = NavySurface,
  onSurface = Slate100,
  surfaceVariant = NavyCard,
  onSurfaceVariant = Slate300,
  outline = Slate700,
  error = ErrorRed,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = TealPrimary,
  onPrimary = Color.White,
  primaryContainer = TealLight,
  onPrimaryContainer = TealDark,
  secondary = BlueDeep,
  onSecondary = Color.White,
  secondaryContainer = BlueSky,
  onSecondaryContainer = BlueDeep,
  tertiary = WarningAmber,
  onTertiary = Color.White,
  background = Slate50,
  onBackground = Slate900,
  surface = MedicalWhite,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate700,
  outline = Slate300,
  error = ErrorRed,
  onError = Color.White
)

@Composable
fun CareNexusTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Healthcare theme default
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
