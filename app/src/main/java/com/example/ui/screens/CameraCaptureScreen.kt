package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppSizes

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraCaptureScreen(
  pageCount: Int,
  onImageCaptured: (Uri) -> Unit,
  onFinishCapture: () -> Unit,
  onBack: () -> Unit
) {
  val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

  LaunchedEffect(Unit) {
    if (!cameraPermissionState.status.isGranted) {
      cameraPermissionState.launchPermissionRequest()
    }
  }

  if (!cameraPermissionState.status.isGranted) {
    Surface(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .safeDrawingPadding()
          .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.NoPhotography,
          contentDescription = null,
          modifier = Modifier.size(64.dp),
          tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Camera Permission Required",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Pic to PDF needs camera access to scan documents.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { cameraPermissionState.launchPermissionRequest() }, modifier = Modifier.heightIn(min = AppSizes.buttonHeight)) {
          Text("Grant Permission")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBack) {
          Text("Go Back")
        }
      }
    }
  } else {
    CameraPreviewContent(
      pageCount = pageCount,
      onImageCaptured = onImageCaptured,
      onFinishCapture = onFinishCapture,
      onBack = onBack
    )
  }
}

@Composable
fun CameraPreviewContent(
  pageCount: Int,
  onImageCaptured: (Uri) -> Unit,
  onFinishCapture: () -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
  var flashEnabled by remember { mutableStateOf(false) }
  var capturing by remember { mutableStateOf(false) }
  var captureError by remember { mutableStateOf<String?>(null) }
  val flashOpacity = remember { androidx.compose.animation.core.Animatable(0f) }
  val sound = remember { android.media.MediaActionSound().apply { load(android.media.MediaActionSound.SHUTTER_CLICK) } }
  LaunchedEffect(capturing) {
    if (capturing) {
      flashOpacity.snapTo(0.85f)
      flashOpacity.animateTo(0f, androidx.compose.animation.core.tween(220))
    }
  }
  val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

  DisposableEffect(Unit) {
    onDispose {
      cameraExecutor.shutdown()
      sound.release()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    AndroidView(
      factory = { ctx ->
        val previewView = PreviewView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
          val cameraProvider = cameraProviderFuture.get()
          val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
          }

          imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

          val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

          try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
              lifecycleOwner,
              cameraSelector,
              preview,
              imageCapture
            )
            imageCapture?.flashMode = if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }, ContextCompat.getMainExecutor(context))
        previewView
      },
      modifier = Modifier.fillMaxSize(),
      onRelease = { ProcessCameraProvider.getInstance(context).get().unbindAll() }
    )
    Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = flashOpacity.value)))
    captureError?.let { message ->
      AlertDialog(onDismissRequest = { captureError = null }, title = { Text("Capture failed") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { captureError = null }) { Text("OK") } })
    }

    // Document framing guide overlay
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(48.dp),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.75f)
          .border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
      )
    }

    // Top control bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(AppSpacing.md),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
      ) {
        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
      }

      IconButton(
        onClick = {
          flashEnabled = !flashEnabled
          imageCapture?.flashMode = if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        },
        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
      ) {
        Icon(
          imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
          contentDescription = "Flash",
          tint = Color.White
        )
      }
    }

    // Bottom control bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .background(Color.Black.copy(alpha = 0.6f))
        .navigationBarsPadding()
        .padding(AppSpacing.md),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Page count indicator
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.primary
        ) {
          Text(
            text = "$pageCount pages",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
          )
        }

        // Capture Button
        IconButton(
          onClick = {
            val capture = imageCapture ?: return@IconButton
            if (capturing) return@IconButton
            capturing = true
            sound.play(android.media.MediaActionSound.SHUTTER_CLICK)
            val photoFile = File(
              context.cacheDir,
              "scan_${System.currentTimeMillis()}.jpg"
            )
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            capture.takePicture(
              outputOptions,
              cameraExecutor,
              object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                  val savedUri = Uri.fromFile(photoFile)
                  ContextCompat.getMainExecutor(context).execute {
                    capturing = false
                    onImageCaptured(savedUri)
                  }
                }

                override fun onError(exception: ImageCaptureException) {
                  ContextCompat.getMainExecutor(context).execute {
                    capturing = false
                    captureError = exception.message ?: "Please try again."
                  }
                }
              }
            )
          },
          modifier = Modifier
            .size(64.dp)
            .border(4.dp, Color.White, CircleShape)
            .padding(4.dp)
            .clip(CircleShape)
            .background(Color.White)
        ) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Camera,
              contentDescription = "Capture",
              tint = Color.Black,
              modifier = Modifier.size(36.dp)
            )
          }
        }

        // Done / Review Button
        Button(
          onClick = onFinishCapture,
          enabled = pageCount > 0,
          modifier = Modifier.heightIn(min = AppSizes.buttonHeight),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Done")
        }
      }
    }
  }
}
