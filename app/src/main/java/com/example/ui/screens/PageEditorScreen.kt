package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.model.FilterType
import com.example.domain.model.ScanPage
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppSizes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageEditorScreen(
  pages: List<ScanPage>,
  onAddMoreCamera: () -> Unit,
  onAddMoreGallery: (List<Uri>) -> Unit,
  onRemovePage: (String) -> Unit,
  onRotatePage: (String) -> Unit,
  onUpdateFilter: (String, FilterType) -> Unit,
  onReorder: (Int, Int) -> Unit,
  onProceedToPdfSettings: () -> Unit,
  onBack: () -> Unit
) {
  var selectedPageIndex by remember { mutableStateOf(0) }

  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    if (uris.isNotEmpty()) {
      onAddMoreGallery(uris)
    }
  }

  if (pages.isEmpty()) {
    Surface(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .safeDrawingPadding()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
          contentDescription = null,
          modifier = Modifier.size(64.dp),
          tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "No pages in session",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack, modifier = Modifier.heightIn(min = AppSizes.buttonHeight)) {
          Text("Go Back")
        }
      }
    }
    return
  }

  val activePageIndex = selectedPageIndex.coerceIn(0, pages.size - 1)
  val activePage = pages[activePageIndex]

  Scaffold(
    containerColor = androidx.compose.ui.graphics.Color.Transparent,
    topBar = {
      TopAppBar(
        title = { Text("Edit ${if (pages.size == 1) "Page" else "Pages"} (${pages.size})") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          Button(
            onClick = onProceedToPdfSettings,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(end = 8.dp).heightIn(min = AppSizes.buttonHeight)
          ) {
            Text("PDF Settings", maxLines = 1)
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(AppSpacing.screen),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Main preview box
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = activePage.uriString,
          contentDescription = "Page preview",
          modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = activePage.rotation.toFloat() },
          contentScale = ContentScale.Fit
        )

        // Overlay page info badge
        Surface(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp),
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ) {
          Text(
            text = "Page ${activePageIndex + 1} of ${pages.size}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(AppSpacing.md))

      Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        FilledTonalIconButton(onClick = onAddMoreCamera) { Icon(Icons.Default.AddAPhoto, "Add with camera") }
        FilledTonalIconButton(onClick = { galleryLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Icon(Icons.Default.AddPhotoAlternate, "Add photos") }
        FilledTonalIconButton(onClick = { onRotatePage(activePage.id) }) { Icon(Icons.AutoMirrored.Filled.RotateRight, "Rotate page") }
        FilledTonalIconButton(onClick = { onRemovePage(activePage.id); selectedPageIndex = (activePageIndex - 1).coerceAtLeast(0) }) { Icon(Icons.Default.Delete, "Delete page") }
      }

      // Thumbnails lazy row
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .height(88.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        itemsIndexed(pages) { index, page ->
          val isSelected = index == activePageIndex
          Box(
            modifier = Modifier
              .size(68.dp, 88.dp)
              .clip(RoundedCornerShape(8.dp))
              .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
              )
              .clickable { selectedPageIndex = index }
          ) {
            AsyncImage(
              model = page.uriString,
              contentDescription = "Thumbnail $index",
              modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = page.rotation.toFloat() },
              contentScale = ContentScale.Crop
            )
            Surface(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp),
              shape = RoundedCornerShape(4.dp),
              color = Color.Black.copy(alpha = 0.6f)
            ) {
              Text(
                text = "${index + 1}",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier.heightIn(min = AppSizes.buttonHeight),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    )
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    )
  }
}
