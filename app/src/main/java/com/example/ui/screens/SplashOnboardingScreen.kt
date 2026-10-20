package com.example.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import kotlinx.coroutines.delay
@Composable
fun SplashOnboardingScreen(onFinish: () -> Unit) {
  val finish by rememberUpdatedState(onFinish)
  LaunchedEffect(Unit) { delay(3000); finish() }
  BoxWithConstraints(Modifier.fillMaxSize().background(com.example.ui.theme.BrandBackground).safeDrawingPadding()) {
    // Double the bitmap while reserving space for its artwork and the captions.
    // The supplied image includes generous blank margins around the illustration.
    val originalSide = minOf(maxWidth * 0.82f, 360.dp, ((maxHeight - 100.dp) / 1.3f).coerceAtLeast(1.dp))
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
      Box(Modifier.fillMaxWidth().height(originalSide * 1.3f).clipToBounds(), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.app_logo), null, Modifier.requiredSize(originalSide * 2f))
      }
      Text("Pic to PDF", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFF242424))
      Spacer(Modifier.height(12.dp))
      Text("Snap or Select. Save as PDF", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF555555))
    }
  }
}
