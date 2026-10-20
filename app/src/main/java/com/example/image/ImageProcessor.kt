package com.example.image

import android.content.Context
import android.graphics.*
import android.net.Uri
import com.example.domain.model.FilterType
import com.example.domain.model.ScanPage
import java.io.InputStream

object ImageProcessor {

  fun loadAndProcessBitmap(context: Context, page: ScanPage, maxDimension: Int = 2048): Bitmap? {
    val uri = Uri.parse(page.uriString)
    val bitmap = decodeSampledBitmapFromUri(context, uri, maxDimension) ?: return null

    // 1. Rotate
    val rotated = if (page.rotation != 0) {
      val matrix = Matrix().apply { postRotate(page.rotation.toFloat()) }
      Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
        if (it != bitmap) bitmap.recycle()
      }
    } else {
      bitmap
    }

    // 2. Crop
    val width = rotated.width
    val height = rotated.height
    val cropX = (page.cropLeft * width).toInt().coerceIn(0, width - 1)
    val cropY = (page.cropTop * height).toInt().coerceIn(0, height - 1)
    val cropW = ((page.cropRight - page.cropLeft) * width).toInt().coerceIn(1, width - cropX)
    val cropH = ((page.cropBottom - page.cropTop) * height).toInt().coerceIn(1, height - cropY)

    val cropped = try {
      Bitmap.createBitmap(rotated, cropX, cropY, cropW, cropH).also {
        if (it != rotated) rotated.recycle()
      }
    } catch (e: Exception) {
      rotated
    }

    // 3. Filter
    return applyFilter(cropped, page.filterType)
  }

  private fun decodeSampledBitmapFromUri(context: Context, uri: Uri, maxDim: Int): Bitmap? {
    try {
      // First decode with inJustDecodeBounds=true to check dimensions
      val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
      }

      options.inSampleSize = calculateInSampleSize(options, maxDim, maxDim)
      options.inJustDecodeBounds = false

      context.contentResolver.openInputStream(uri)?.use { stream ->
        return BitmapFactory.decodeStream(stream, null, options)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return null
  }

  private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
      val halfHeight = height / 2
      val halfWidth = width / 2
      while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }

  private fun applyFilter(bitmap: Bitmap, filterType: FilterType): Bitmap {
    if (filterType == FilterType.ORIGINAL) return bitmap

    val width = bitmap.width
    val height = bitmap.height
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint()

    val colorMatrix = ColorMatrix()
    when (filterType) {
      FilterType.GRAYSCALE -> {
        colorMatrix.setSaturation(0f)
      }
      FilterType.BLACK_WHITE -> {
        colorMatrix.setSaturation(0f)
        // Increase contrast for B&W document scan
        val scale = 2f
        val translate = -128f * scale + 128f
        val array = floatArrayOf(
          scale, 0f, 0f, 0f, translate,
          0f, scale, 0f, 0f, translate,
          0f, 0f, scale, 0f, translate,
          0f, 0f, 0f, 1f, 0f
        )
        colorMatrix.set(array)
      }
      else -> {}
    }

    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    if (bitmap != output) bitmap.recycle()
    return output
  }
}
