package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DocumentDatabase
import com.example.data.local.PdfDocumentEntity
import com.example.data.preferences.UserPreferences
import com.example.domain.model.*
import com.example.pdf.PdfGenerator
import com.example.pdf.PdfGenerationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val database = DocumentDatabase.getDatabase(application)
  private val dao = database.pdfDocumentDao()
  private val userPreferences = UserPreferences(application)

  val allDocuments: StateFlow<List<PdfDocumentEntity>> =
    dao.getAllDocuments()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val onboardingCompleted: StateFlow<Boolean> =
    userPreferences.onboardingCompleted
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  enum class ScreenState {
    SPLASH,
    HOME,
    CAMERA,
    EDITOR,
    PDF_SETTINGS,
    PREVIEW,
    LIBRARY,
    SETTINGS
  }

  val currentScreen = MutableStateFlow(ScreenState.SPLASH)
  var previewFilePath: String = ""
  var previewTitle: String = ""

  // Current session editing state
  private val _sessionPages = MutableStateFlow<List<ScanPage>>(emptyList())
  val sessionPages: StateFlow<List<ScanPage>> = _sessionPages.asStateFlow()

  private val _pdfSettings = MutableStateFlow(PdfSettings())
  val pdfSettings: StateFlow<PdfSettings> = _pdfSettings.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  private val _generationResult = MutableStateFlow<PdfGenerationResult?>(null)
  val generationResult: StateFlow<PdfGenerationResult?> = _generationResult.asStateFlow()

  init {
    // Set default filename with timestamp
    updateFileName(getDefaultFileName())
  }

  fun completeOnboarding() {
    viewModelScope.launch {
      userPreferences.setOnboardingCompleted(true)
    }
  }

  fun addPage(uri: Uri) {
    // Copy uri to app-private cache file for persistence and stability
    val context = getApplication<Application>()
    val copiedUriString = copyUriToPrivateStorage(context, uri) ?: uri.toString()
    val newPage = ScanPage(uriString = copiedUriString)
    _sessionPages.value = _sessionPages.value + newPage
  }

  fun addPages(uris: List<Uri>) {
    val context = getApplication<Application>()
    val newPages = uris.map { uri ->
      val copied = copyUriToPrivateStorage(context, uri) ?: uri.toString()
      ScanPage(uriString = copied)
    }
    _sessionPages.value = _sessionPages.value + newPages
  }

  fun removePage(pageId: String) {
    _sessionPages.value = _sessionPages.value.filter { it.id != pageId }
  }

  fun reorderPages(fromIndex: Int, toIndex: Int) {
    val list = _sessionPages.value.toMutableList()
    if (fromIndex in list.indices && toIndex in list.indices) {
      val item = list.removeAt(fromIndex)
      list.add(toIndex, item)
      _sessionPages.value = list
    }
  }

  fun rotatePage(pageId: String) {
    _sessionPages.value = _sessionPages.value.map { page ->
      if (page.id == pageId) {
        page.copy(rotation = (page.rotation + 90) % 360)
      } else {
        page
      }
    }
  }

  fun updatePageFilter(pageId: String, filter: FilterType) {
    _sessionPages.value = _sessionPages.value.map { page ->
      if (page.id == pageId) {
        page.copy(filterType = filter)
      } else {
        page
      }
    }
  }

  fun updatePageCrop(pageId: String, left: Float, top: Float, right: Float, bottom: Float) {
    _sessionPages.value = _sessionPages.value.map { page ->
      if (page.id == pageId) {
        page.copy(cropLeft = left, cropTop = top, cropRight = right, cropBottom = bottom)
      } else {
        page
      }
    }
  }

  fun updatePdfSettings(updater: (PdfSettings) -> PdfSettings) {
    _pdfSettings.value = updater(_pdfSettings.value)
  }

  fun updateFileName(name: String) {
    _pdfSettings.value = _pdfSettings.value.copy(customFileName = name)
  }

  fun clearSession() {
    _sessionPages.value = emptyList()
    _generationResult.value = null
    updateFileName(getDefaultFileName())
  }

  fun generatePdfToCache(context: Context, onComplete: (File?) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      _isGenerating.value.let { _isGenerating.value = true }
      try {
        val pages = _sessionPages.value
        val settings = _pdfSettings.value
        val fileName = if (settings.customFileName.isBlank()) getDefaultFileName() else settings.customFileName
        val sanitizedName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"

        val cacheDir = File(context.cacheDir, "generated_pdfs").apply { mkdirs() }
        val outputFile = File(cacheDir, sanitizedName)

        FileOutputStream(outputFile).use { fos ->
          val result = PdfGenerator.generatePdf(context, pages, settings, fos)
          if (result.success) {
            val fileLength = outputFile.length()
            // Save to Room database
            dao.insertDocument(
              PdfDocumentEntity(
                title = sanitizedName,
                filePath = outputFile.absolutePath,
                pageCount = result.pageCount,
                fileSize = fileLength,
                createdAt = System.currentTimeMillis()
              )
            )
            val successResult = result.copy(filePath = outputFile.absolutePath)
            _generationResult.value = successResult
            onComplete(outputFile)
          } else {
            _generationResult.value = result
            onComplete(null)
          }
        }
      } catch (e: Exception) {
        _generationResult.value = PdfGenerationResult(success = false, errorMessage = e.localizedMessage)
        onComplete(null)
      } finally {
        _isGenerating.value = false
      }
    }
  }

  fun deleteDocument(id: Long) {
    viewModelScope.launch(Dispatchers.IO) {
      dao.deleteDocumentById(id)
    }
  }

  private fun copyUriToPrivateStorage(context: Context, uri: Uri): String? {
    try {
      val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
      val fileDir = File(context.filesDir, "scan_images").apply { mkdirs() }
      val outputFile = File(fileDir, "scan_${System.currentTimeMillis()}_${Math.random().toString().take(4)}.jpg")
      FileOutputStream(outputFile).use { fos ->
        inputStream.copyTo(fos)
      }
      return Uri.fromFile(outputFile).toString()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return null
  }

  private fun getDefaultFileName(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault())
    return "PicToPDF_${sdf.format(Date())}"
  }
}
