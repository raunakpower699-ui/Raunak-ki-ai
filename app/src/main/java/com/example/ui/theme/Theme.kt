package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val JarvisDarkColorScheme = darkColorScheme(
  primary = CyberCyan,
  onPrimary = Color(0xFF030712),
  primaryContainer = Color(0xFF003847),
  onPrimaryContainer = Color(0xFFA5F3FC),
  secondary = ElectricViolet,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF3B0764),
  onSecondaryContainer = Color(0xFFF3E8FF),
  tertiary = NeonPink,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF500724),
  onTertiaryContainer = Color(0xFFFFD6E7),
  background = SpaceObsidian,
  onBackground = TextPrimary,
  surface = GlassSurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = GlassSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = GlassBorderCyan
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // JARVIS default is dark cybernetic aesthetic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = JarvisDarkColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
