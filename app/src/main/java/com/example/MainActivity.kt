package com.example

import android.os.Bundle
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val preferences = remember { com.example.data.preferences.UserPreferences(applicationContext) }
      val themeMode by preferences.darkThemeMode.collectAsStateWithLifecycle(initialValue = 0)
      val scope = rememberCoroutineScope()
      MyApplicationTheme(darkTheme = when (themeMode) { 1 -> false; 2 -> true; else -> androidx.compose.foundation.isSystemInDarkTheme() }) {
        val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
        val sessionPages by viewModel.sessionPages.collectAsStateWithLifecycle()
        val pdfSettings by viewModel.pdfSettings.collectAsStateWithLifecycle()
        val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
        val documents by viewModel.allDocuments.collectAsStateWithLifecycle()

        val activeScreen = currentScreen
        com.example.ui.theme.AnimatedBackground {
          when (activeScreen) {
            MainViewModel.ScreenState.SPLASH -> {
              SplashOnboardingScreen(
                onFinish = {
                  viewModel.completeOnboarding()
                  viewModel.currentScreen.value = MainViewModel.ScreenState.HOME
                }
              )
            }
            MainViewModel.ScreenState.HOME -> {
              HomeScreen(
                documents = documents,
                onStartCamera = { viewModel.currentScreen.value = MainViewModel.ScreenState.CAMERA },
                onImagesSelected = { uris ->
                  viewModel.addPages(uris)
                  viewModel.currentScreen.value = MainViewModel.ScreenState.EDITOR
                },
                onOpenDocument = { doc ->
                  viewModel.previewFilePath = doc.filePath
                  viewModel.previewTitle = doc.title
                  viewModel.currentScreen.value = MainViewModel.ScreenState.PREVIEW
                },
                onDeleteDocument = { id -> viewModel.deleteDocument(id) },
                onOpenLibrary = { viewModel.currentScreen.value = MainViewModel.ScreenState.LIBRARY },
                onOpenSettings = { viewModel.currentScreen.value = MainViewModel.ScreenState.SETTINGS }
              )
            }
            MainViewModel.ScreenState.CAMERA -> {
              BackHandler { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              CameraCaptureScreen(
                pageCount = sessionPages.size,
                onImageCaptured = { uri -> viewModel.addPage(uri) },
                onFinishCapture = {
                  if (sessionPages.isNotEmpty()) {
                    viewModel.currentScreen.value = MainViewModel.ScreenState.EDITOR
                  } else {
                    viewModel.currentScreen.value = MainViewModel.ScreenState.HOME
                  }
                },
                onBack = { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              )
            }
            MainViewModel.ScreenState.EDITOR -> {
              BackHandler { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              PageEditorScreen(
                pages = sessionPages,
                onAddMoreCamera = { viewModel.currentScreen.value = MainViewModel.ScreenState.CAMERA },
                onAddMoreGallery = { uris -> viewModel.addPages(uris) },
                onRemovePage = { id -> viewModel.removePage(id) },
                onRotatePage = { id -> viewModel.rotatePage(id) },
                onUpdateFilter = { id, filter -> viewModel.updatePageFilter(id, filter) },
                onReorder = { from, to -> viewModel.reorderPages(from, to) },
                onProceedToPdfSettings = { viewModel.currentScreen.value = MainViewModel.ScreenState.PDF_SETTINGS },
                onBack = { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              )
            }
            MainViewModel.ScreenState.PDF_SETTINGS -> {
              BackHandler { viewModel.currentScreen.value = MainViewModel.ScreenState.EDITOR }
              PdfSettingsScreen(
                settings = pdfSettings,
                pageCount = sessionPages.size,
                isGenerating = isGenerating,
                onUpdateSettings = { updater -> viewModel.updatePdfSettings(updater) },
                onUpdateFileName = { name -> viewModel.updateFileName(name) },
                onGenerateAndSave = { context, uri ->
                  viewModel.generatePdfToCache(context) { file ->
                    if (file != null) {
                      try {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                          file.inputStream().use { input -> input.copyTo(output) }
                        }
                        viewModel.previewFilePath = file.absolutePath
                        viewModel.previewTitle = pdfSettings.customFileName.ifBlank { "Generated_PDF.pdf" }
                        viewModel.currentScreen.value = MainViewModel.ScreenState.PREVIEW
                      } catch (e: Exception) {
                        e.printStackTrace()
                      }
                    }
                  }
                },
                onBack = { viewModel.currentScreen.value = MainViewModel.ScreenState.EDITOR }
              )
            }
            MainViewModel.ScreenState.PREVIEW -> {
              BackHandler { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              PdfPreviewScreen(
                filePath = viewModel.previewFilePath,
                title = viewModel.previewTitle,
                onShare = { _, _ -> },
                onBack = { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              )
            }
            MainViewModel.ScreenState.LIBRARY -> {
              BackHandler { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              DocumentsLibraryScreen(
                documents = documents,
                onOpenDocument = { doc ->
                  viewModel.previewFilePath = doc.filePath
                  viewModel.previewTitle = doc.title
                  viewModel.currentScreen.value = MainViewModel.ScreenState.PREVIEW
                },
                onDeleteDocument = { id -> viewModel.deleteDocument(id) },
                onBack = { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              )
            }
            MainViewModel.ScreenState.SETTINGS -> {
              BackHandler { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              SettingsScreen(
                themeMode = themeMode,
                onThemeChange = { mode -> scope.launch { preferences.setDarkThemeMode(mode) } },
                onBack = { viewModel.currentScreen.value = MainViewModel.ScreenState.HOME }
              )
            }
          }
        }
      }
    }
  }
}
