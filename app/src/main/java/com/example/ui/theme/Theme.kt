package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
  extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
  small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
  medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
  large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
  extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF173B69),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFD6E4FF),
    secondary = SecondaryTeal,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF164D48),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFCCF2EB),
    tertiary = TertiaryAmber,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF493800),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFE08A),
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF292B30),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC4C6CF),
    outline = androidx.compose.ui.graphics.Color(0xFF8B9099),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF43464D)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFD6E4FF),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF001C3A),
    secondary = SecondaryTeal,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFBCEDE4),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF00201B),
    tertiary = TertiaryAmber,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFE08A),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF251A00),
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE9EDF2),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF42474F),
    outline = androidx.compose.ui.graphics.Color(0xFF747982),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFC3C7CF)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = AppShapes, content = content)
}
