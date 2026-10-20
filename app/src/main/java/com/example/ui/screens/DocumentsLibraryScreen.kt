package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.PdfDocumentEntity
import com.example.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsLibraryScreen(
  documents: List<PdfDocumentEntity>,
  onOpenDocument: (PdfDocumentEntity) -> Unit,
  onDeleteDocument: (Long) -> Unit,
  onBack: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredDocs = remember(documents, searchQuery) {
    if (searchQuery.isBlank()) documents
    else documents.filter { it.title.contains(searchQuery, ignoreCase = true) }
  }

  Scaffold(
    containerColor = androidx.compose.ui.graphics.Color.Transparent,
    topBar = {
      TopAppBar(
        title = { Text("Document Library") },
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
        .padding(AppSpacing.screen),
      verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search Documents by Title") },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true
      )

      if (filteredDocs.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .weight(1f),
          contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
          Text("No documents found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
          items(filteredDocs) { doc ->
            DocumentItemCard(
              document = doc,
              onClick = { onOpenDocument(doc) },
              onDelete = { onDeleteDocument(doc.id) }
            )
          }
        }
      }
    }
  }
}
