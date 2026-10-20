package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppSizes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PdfSettingsScreen(
  settings: PdfSettings,
  pageCount: Int,
  isGenerating: Boolean,
  onUpdateSettings: ((PdfSettings) -> PdfSettings) -> Unit,
  onUpdateFileName: (String) -> Unit,
  onGenerateAndSave: (Context, Uri) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // SAF create document launcher
  val savePdfLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    if (uri != null) {
      onGenerateAndSave(context, uri)
    }
  }

  Scaffold(
    containerColor = androidx.compose.ui.graphics.Color.Transparent,
    topBar = {
      TopAppBar(
        title = { Text("PDF Settings & Export") },
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
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text("$pageCount ${if (pageCount == 1) "page" else "pages"} ready to export", style = MaterialTheme.typography.bodyMedium)
      // File Name input
      OutlinedTextField(
        value = settings.customFileName,
        onValueChange = onUpdateFileName,
        label = { Text("PDF File Name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
      )

      // Page Size Section
      Text("Page Size", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
      ) {
        PageSizeOption.values().forEach { option ->
          CompactOption(
            selected = settings.pageSize == option,
            onClick = { onUpdateSettings { it.copy(pageSize = option) } },
            label = { Text(when(option.name) { "ORIGINAL" -> "Orig"; "SMALL_FILE" -> "Small"; "HIGH_QUALITY" -> "High"; else -> option.name.lowercase().replaceFirstChar { it.uppercase() } }, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Orientation Section
      Text("Page Orientation", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
      ) {
        OrientationOption.values().forEach { option ->
          CompactOption(
            selected = settings.orientation == option,
            onClick = { onUpdateSettings { it.copy(orientation = option) } },
            label = { Text(when(option.name) { "ORIGINAL" -> "Orig"; "SMALL_FILE" -> "Small"; "HIGH_QUALITY" -> "High"; else -> option.name.lowercase().replaceFirstChar { it.uppercase() } }, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Image Fitting Section
      Text("Image Fitting", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        FittingOption.values().forEach { option ->
          CompactOption(selected = settings.fitting == option, onClick = { onUpdateSettings { it.copy(fitting = option) } }, label = { Text(when(option) { FittingOption.FIT_TO_PAGE -> "Fit"; FittingOption.FILL_PAGE -> "Fill"; FittingOption.ORIGINAL_PROPORTIONS -> "Proportions" }, style = MaterialTheme.typography.labelSmall, maxLines = 1) }, modifier = Modifier.weight(1f))
        }
      }

      // Quality Section
      Text("PDF Quality Preset", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
      ) {
        QualityOption.values().forEach { option ->
          CompactOption(
            selected = settings.quality == option,
            onClick = { onUpdateSettings { it.copy(quality = option) } },
            label = { Text(when(option.name) { "ORIGINAL" -> "Orig"; "SMALL_FILE" -> "Small"; "HIGH_QUALITY" -> "High"; else -> option.name.lowercase().replaceFirstChar { it.uppercase() } }, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.weight(1f),
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Generate & Save Button
      Button(
        onClick = {
          val suggestedName = if (settings.customFileName.isBlank()) "PicToPDF_Document.pdf" else {
            if (settings.customFileName.endsWith(".pdf", ignoreCase = true)) settings.customFileName else "${settings.customFileName}.pdf"
          }
          savePdfLauncher.launch(suggestedName)
        },
        enabled = !isGenerating && pageCount > 0,
        modifier = Modifier
          .fillMaxWidth()
          .height(AppSizes.buttonHeight)
      ) {
        if (isGenerating) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
          Spacer(modifier = Modifier.width(12.dp))
          Text("Generating PDF...")
        } else {
          Icon(Icons.Default.Save, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Generate & Save PDF", style = MaterialTheme.typography.titleMedium)
        }
      }
    }
  }
}

@Composable
private fun CompactOption(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit, modifier: Modifier = Modifier) {
  Surface(onClick = onClick, modifier = modifier.heightIn(min = 48.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.65f), border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
    Box(Modifier.padding(horizontal = 2.dp, vertical = 4.dp), contentAlignment = Alignment.Center) { label() }
  }
}
