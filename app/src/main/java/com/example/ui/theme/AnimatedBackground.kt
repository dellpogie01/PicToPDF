package com.example.ui.theme
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val BrandBackground = Color(0xFFF8F8F6)

@Composable
fun AnimatedBackground(content: @Composable () -> Unit) {
  val transition = rememberInfiniteTransition(label = "Background")
  val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Gradient motion")
  val scheme = MaterialTheme.colorScheme
  val colors = listOf(
    lerp(scheme.background, scheme.primaryContainer, 0.25f + phase * 0.35f),
    lerp(scheme.secondaryContainer, scheme.tertiaryContainer, phase),
    lerp(scheme.primaryContainer, scheme.background, phase)
  )
  Box(Modifier.fillMaxSize()) {
    Canvas(Modifier.fillMaxSize()) {
      drawRect(Brush.linearGradient(colors, Offset(-size.width * phase, 0f), Offset(size.width * (1.5f - phase), size.height)))
    }
    content()
  }
}
