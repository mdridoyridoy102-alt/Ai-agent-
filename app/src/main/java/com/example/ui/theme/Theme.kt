package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StudioDarkColorScheme = darkColorScheme(
  primary = Indigo500,
  onPrimary = Slate50,
  primaryContainer = Indigo600,
  onPrimaryContainer = Slate100,
  secondary = Cyan400,
  onSecondary = Slate950,
  secondaryContainer = Slate800,
  onSecondaryContainer = Cyan400,
  tertiary = Emerald400,
  onTertiary = Slate950,
  background = Slate950,
  onBackground = Slate100,
  surface = Slate900,
  onSurface = Slate100,
  surfaceVariant = Slate800,
  onSurfaceVariant = Slate300,
  outline = Slate700,
  outlineVariant = Slate800,
  error = Rose500,
  onError = Slate50
)

private val StudioLightColorScheme = lightColorScheme(
  primary = Indigo600,
  onPrimary = Slate50,
  primaryContainer = Indigo500,
  onPrimaryContainer = Slate50,
  secondary = Cyan500,
  onSecondary = Slate50,
  secondaryContainer = Slate100,
  onSecondaryContainer = Slate800,
  tertiary = Emerald500,
  onTertiary = Slate50,
  background = Slate950, // Keep immersive studio dark by default for IDE look
  onBackground = Slate100,
  surface = Slate900,
  onSurface = Slate100,
  surfaceVariant = Slate800,
  onSurfaceVariant = Slate300,
  outline = Slate700,
  outlineVariant = Slate800,
  error = Rose500,
  onError = Slate50
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to dark studio theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) StudioDarkColorScheme else StudioLightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      window?.let {
        it.statusBarColor = Slate950.toArgb()
        it.navigationBarColor = Slate950.toArgb()
        WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
