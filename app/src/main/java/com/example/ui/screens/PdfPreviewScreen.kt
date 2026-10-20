package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import com.example.ui.theme.appCardColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
  filePath: String,
  title: String,
  onShare: (Uri, String) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var pageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(filePath) {
    try {
      val file = File(filePath)
      if (!file.exists()) {
        errorMessage = "PDF file not found."
        isLoading = false
        return@LaunchedEffect
      }

      val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
      val renderer = PdfRenderer(fileDescriptor)
      val bitmaps = mutableListOf<Bitmap>()

      for (i in 0 until renderer.pageCount) {
        val page = renderer.openPage(i)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        bitmaps.add(bitmap)
        page.close()
      }
      renderer.close()
      fileDescriptor.close()

      pageBitmaps = bitmaps
      isLoading = false
    } catch (e: Exception) {
      errorMessage = e.localizedMessage ?: "Failed to render PDF preview."
      isLoading = false
    }
  }

  val fileUri = remember(filePath) {
    val file = File(filePath)
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
  }

  Scaffold(
    containerColor = androidx.compose.ui.graphics.Color.Transparent,
    topBar = {
      TopAppBar(
        title = { Text(title, maxLines = 1) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = {
            val intent = Intent(Intent.ACTION_SEND).apply {
              type = "application/pdf"
              putExtra(Intent.EXTRA_STREAM, fileUri)
              addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF"))
          }) {
            Icon(Icons.Default.Share, contentDescription = "Share")
          }
        }
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      } else if (errorMessage != null) {
        Text(
          text = errorMessage ?: "Error",
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.align(Alignment.Center)
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          itemsIndexed(pageBitmaps) { index, bitmap ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
              colors = appCardColors()
            ) {
              Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Image(
                  bitmap = bitmap.asImageBitmap(),
                  contentDescription = "Page ${index + 1}",
                  modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(4.dp)),
                  contentScale = ContentScale.FillWidth
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Page ${index + 1} of ${pageBitmaps.size}",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}
