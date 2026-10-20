package com.example.domain.model

enum class FilterType {
  ORIGINAL,
  GRAYSCALE,
  BLACK_WHITE
}

enum class PageSizeOption {
  A4,
  LETTER,
  LEGAL,
  ORIGINAL,
  FIT
}

enum class OrientationOption {
  PORTRAIT,
  LANDSCAPE,
  AUTO
}

enum class FittingOption {
  FIT_TO_PAGE,
  FILL_PAGE,
  ORIGINAL_PROPORTIONS
}

enum class QualityOption {
  SMALL_FILE,
  BALANCED,
  HIGH_QUALITY
}

data class ScanPage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val uriString: String,
  val rotation: Int = 0, // 0, 90, 180, 270
  val filterType: FilterType = FilterType.ORIGINAL,
  val cropLeft: Float = 0f,
  val cropTop: Float = 0f,
  val cropRight: Float = 1f,
  val cropBottom: Float = 1f
)

data class PdfSettings(
  val pageSize: PageSizeOption = PageSizeOption.A4,
  val orientation: OrientationOption = OrientationOption.PORTRAIT,
  val fitting: FittingOption = FittingOption.FIT_TO_PAGE,
  val quality: QualityOption = QualityOption.BALANCED,
  val marginPoints: Float = 20f,
  val customFileName: String = ""
)

data class DocumentItem(
  val id: Long = 0L,
  val title: String,
  val filePath: String,
  val pageCount: Int,
  val fileSize: Long,
  val createdAt: Long = System.currentTimeMillis()
)
