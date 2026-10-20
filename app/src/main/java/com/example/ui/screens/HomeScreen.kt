package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PdfDocumentEntity
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.appCardColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  documents: List<PdfDocumentEntity>,
  onStartCamera: () -> Unit,
  onImagesSelected: (List<Uri>) -> Unit,
  onOpenDocument: (PdfDocumentEntity) -> Unit,
  onDeleteDocument: (Long) -> Unit,
  onOpenLibrary: () -> Unit,
  onOpenSettings: () -> Unit
) {
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    if (uris.isNotEmpty()) {
      onImagesSelected(uris)
    }
  }

  Scaffold(
    containerColor = androidx.compose.ui.graphics.Color.Transparent,
    bottomBar = {
      NavigationBar {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, "Home") }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = onStartCamera, icon = { Box(Modifier.size(28.dp)) {
          Icon(Icons.Default.PhotoCamera, "Camera", Modifier.size(24.dp).align(Alignment.BottomStart))
          Icon(Icons.Default.Add, null, Modifier.size(13.dp).align(Alignment.TopEnd))
        } }, label = { Text("Camera") })
        NavigationBarItem(selected = false, onClick = { galleryLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, icon = { Icon(Icons.Default.AddPhotoAlternate, "Import Photos") }, label = { Text("Gallery") })
        NavigationBarItem(selected = false, onClick = onOpenLibrary, icon = { Icon(Icons.Default.Search, "Search documents", tint = MaterialTheme.colorScheme.onSecondaryContainer) }, label = { Text("Documents") })
      }
    },
    topBar = {
      TopAppBar(
        modifier = Modifier.shadow(8.dp),
        expandedHeight = 104.dp,
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(com.example.R.drawable.app_logo), null, Modifier.size(96.dp))
            // The logo bitmap already includes blank space along its right edge.
            Column(Modifier.weight(1f).offset(x = (-12).dp)) {
              Text("Pic to PDF", fontWeight = FontWeight.Bold)
              Text("From Camera or Gallery to PDF", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
          }
        },
        actions = {
          IconButton(onClick = onOpenSettings) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = com.example.ui.theme.BrandBackground,
          scrolledContainerColor = com.example.ui.theme.BrandBackground,
          titleContentColor = androidx.compose.ui.graphics.Color(0xFF242424),
          actionIconContentColor = androidx.compose.ui.graphics.Color(0xFF242424)
        )
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = AppSpacing.screen),
      verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
      item {
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Documents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          if (documents.isNotEmpty()) {
            TextButton(onClick = onOpenLibrary) {
              Text("View All")
            }
          }
        }
      }

      if (documents.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(192.dp)
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No PDFs created yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Scan or import photos to create your first PDF",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
              )
            }
          }
        }
      } else {
        items(documents.take(5)) { doc ->
          DocumentItemCard(
            document = doc,
            onClick = { onOpenDocument(doc) },
            onDelete = { onDeleteDocument(doc.id) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(AppSpacing.xl))
      }
    }
  }
}

@Composable
fun DocumentItemCard(
  document: PdfDocumentEntity,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }
  var confirmDelete by remember { mutableStateOf(false) }
  if (confirmDelete) {
    AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete PDF") }, text = { Text("Are you sure you want to delete ${if (document.title.endsWith(".pdf", ignoreCase = true)) document.title else "${document.title}.pdf"}? This cannot be undone.") }, confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Yes") } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("No") } })
  }
  val formattedDate = remember(document.createdAt) {
    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(document.createdAt))
  }
  val formattedSize = remember(document.fileSize) {
    String.format(Locale.getDefault(), "%.1f MB", document.fileSize / (1024.0 * 1024.0))
  }

  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    colors = appCardColors()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(AppSpacing.md),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.PictureAsPdf,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
          )
        }
      }

      Spacer(modifier = Modifier.width(AppSpacing.md))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = document.title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${document.pageCount} pages • $formattedSize • $formattedDate",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
      }

      Box {
        IconButton(onClick = { showMenu = true }) {
          Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }
        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Open / Preview") },
            onClick = {
              showMenu = false
              onClick()
            },
            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) }
          )
          DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
              showMenu = false
              confirmDelete = true
            },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
          )
        }
      }
    }
  }
}
