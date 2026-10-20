package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.appCardColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  themeMode: Int,
  onThemeChange: (Int) -> Unit,
  onBack: () -> Unit
) {
  val scrollState = rememberScrollState()

  Scaffold(
    containerColor = androidx.compose.ui.graphics.Color.Transparent,
    topBar = {
      TopAppBar(
        title = { Text("About & Settings") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(scrollState)
        .padding(AppSpacing.screen),
      verticalArrangement = Arrangement.spacedBy(AppSpacing.section)
    ) {
      // Privacy Notice Section
      Text("Privacy & Security", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = appCardColors()
      ) {
        Column(modifier = Modifier.padding(AppSpacing.card)) {
          Text(
            text = "Pic to PDF makes it easy to turn camera shots and gallery images into PDF documents that you can save and share directly from your phone, with all image and PDF processing performed entirely offline on your device, no uploads to remote servers, and no account or cloud database required.",
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }

      Text("Preference", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Light" to 1, "Dark" to 2, "System" to 0).forEach { (label, mode) ->
          Surface(
            onClick = { onThemeChange(mode) },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            shape = MaterialTheme.shapes.small,
            color = if (themeMode == mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
          ) {
            Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) { Text(label) }
          }
        }
      }
    }
  }
}
