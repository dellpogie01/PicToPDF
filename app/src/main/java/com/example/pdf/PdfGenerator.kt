package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.domain.model.FittingOption
import com.example.domain.model.OrientationOption
import com.example.domain.model.PageSizeOption
import com.example.domain.model.PdfSettings
import com.example.domain.model.QualityOption
import com.example.domain.model.ScanPage
import com.example.image.ImageProcessor
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

data class PdfGenerationResult(
  val success: Boolean,
  val filePath: String? = null,
  val pageCount: Int = 0,
  val fileSize: Long = 0L,
  val errorMessage: String? = null
)

object PdfGenerator {

  fun generatePdf(
    context: Context,
    pages: List<ScanPage>,
    settings: PdfSettings,
    outputStream: OutputStream
  ): PdfGenerationResult {
    val pdfDocument = PdfDocument()
    var processedCount = 0

    try {
      for ((index, page) in pages.withIndex()) {
        val maxDim = when (settings.quality) {
          QualityOption.SMALL_FILE -> 1200
          QualityOption.BALANCED -> 1800
          QualityOption.HIGH_QUALITY -> 2500
        }

        val bitmap = ImageProcessor.loadAndProcessBitmap(context, page, maxDim) ?: continue

        val bmpWidth = bitmap.width.toFloat()
        val bmpHeight = bitmap.height.toFloat()

        // Determine page dimensions in points (72 points = 1 inch)
        val (pageWidth, pageHeight) = calculatePageDimensions(settings.pageSize, settings.orientation, bmpWidth, bmpHeight)

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), index + 1).create()
        val pdfPage = pdfDocument.startPage(pageInfo)
        val canvas = pdfPage.canvas

        // Fill white background
        canvas.drawColor(android.graphics.Color.WHITE)

        val margin = settings.marginPoints
        val printableWidth = pageWidth - (2 * margin)
        val printableHeight = pageHeight - (2 * margin)

        // Calculate drawing rectangle based on fitting option
        val destRect = calculateDestinationRect(
          bmpWidth, bmpHeight,
          margin, margin, printableWidth, printableHeight,
          settings.fitting
        )

        val paint = Paint().apply {
          isAntiAlias = true
          isFilterBitmap = true
          isDither = true
        }

        canvas.drawBitmap(bitmap, null, destRect, paint)
        pdfDocument.finishPage(pdfPage)
        bitmap.recycle()
        processedCount++
      }

      if (processedCount == 0) {
        pdfDocument.close()
        return PdfGenerationResult(success = false, errorMessage = "No valid pages to generate PDF.")
      }

      pdfDocument.writeTo(outputStream)
      pdfDocument.close()
      return PdfGenerationResult(success = true, pageCount = processedCount)
    } catch (e: Exception) {
      pdfDocument.close()
      return PdfGenerationResult(success = false, errorMessage = e.localizedMessage ?: "Unknown error during PDF generation")
    }
  }

  private fun calculatePageDimensions(
    pageSize: PageSizeOption,
    orientation: OrientationOption,
    bmpW: Float,
    bmpH: Float
  ): Pair<Float, Float> {
    var (width, height) = when (pageSize) {
      PageSizeOption.A4 -> Pair(595.28f, 841.89f)
      PageSizeOption.LETTER -> Pair(612f, 792f)
      PageSizeOption.LEGAL -> Pair(612f, 1008f)
      PageSizeOption.ORIGINAL, PageSizeOption.FIT -> Pair(bmpW, bmpH)
    }

    if (pageSize != PageSizeOption.ORIGINAL && pageSize != PageSizeOption.FIT) {
      val isLandscapeBmp = bmpW > bmpH
      val isLandscapePage = when (orientation) {
        OrientationOption.LANDSCAPE -> true
        OrientationOption.PORTRAIT -> false
        OrientationOption.AUTO -> isLandscapeBmp
      }

      if (isLandscapePage && width < height) {
        val temp = width
        width = height
        height = temp
      } else if (!isLandscapePage && width > height) {
        val temp = width
        width = height
        height = temp
      }
    }
    return Pair(width, height)
  }

  private fun calculateDestinationRect(
    bmpW: Float,
    bmpH: Float,
    leftMargin: Float,
    topMargin: Float,
    pWidth: Float,
    pHeight: Float,
    fitting: FittingOption
  ): android.graphics.RectF {
    val bmpAspect = bmpW / bmpH
    val pageAspect = pWidth / pHeight

    var destW = pWidth
    var destH = pHeight

    when (fitting) {
      FittingOption.FIT_TO_PAGE -> {
        if (bmpAspect > pageAspect) {
          destW = pWidth
          destH = pWidth / bmpAspect
        } else {
          destH = pHeight
          destW = pHeight * bmpAspect
        }
      }
      FittingOption.FILL_PAGE -> {
        if (bmpAspect > pageAspect) {
          destH = pHeight
          destW = pHeight * bmpAspect
        } else {
          destW = pWidth
          destH = pWidth / bmpAspect
        }
      }
      FittingOption.ORIGINAL_PROPORTIONS -> {
        destW = bmpW.coerceAtMost(pWidth)
        destH = bmpH.coerceAtMost(pHeight)
      }
    }

    val left = leftMargin + (pWidth - destW) / 2f
    val top = topMargin + (pHeight - destH) / 2f
    return android.graphics.RectF(left, top, left + destW, top + destH)
  }
}
