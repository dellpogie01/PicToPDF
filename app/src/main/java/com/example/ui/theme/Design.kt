package com.example.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Shared layout and control dimensions used throughout the app. */
object AppSpacing {
  val xxs = 4.dp
  val xs = 8.dp
  val sm = 12.dp
  val md = 16.dp
  val lg = 24.dp
  val xl = 32.dp
  val screen = 20.dp
  val section = 24.dp
  val card = 16.dp
}

object AppSizes {
  val buttonHeight = 52.dp
  val textFieldHeight = 56.dp
  val icon = 24.dp
  val iconTouchTarget = 48.dp
  val cardCorner = 16.dp
  val controlCorner = 12.dp
}

@Composable
fun appCardColors() = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
