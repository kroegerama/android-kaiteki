package com.kroegerama.kaiteki

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.FileDescriptor
import java.text.SimpleDateFormat
import kotlin.math.roundToInt

fun ExifInterface.getRotationMatrix() =
        when (getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> Matrix().apply { postRotate(90f) }   //6
            ExifInterface.ORIENTATION_ROTATE_180 -> Matrix().apply { postRotate(180f) } //3
            ExifInterface.ORIENTATION_ROTATE_270 -> Matrix().apply { postRotate(270f) } //8

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> Matrix().apply { postScale(-1f, 1f) } //2
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> Matrix().apply { postScale(1f, -1f) } //4

            ExifInterface.ORIENTATION_TRANSPOSE -> Matrix().apply { postRotate(90f); postScale(-1f, 1f) } //5
            ExifInterface.ORIENTATION_TRANSVERSE -> Matrix().apply { postRotate(270f); postScale(-1f, 1f) } //7

            else -> null
        }

@SuppressLint("SimpleDateFormat")
fun ExifInterface.getDateTimeAsDate() =
        try {
            getAttribute(ExifInterface.TAG_DATETIME)?.let { attr ->
                // JavaDoc from ExifInterface.TAG_DATETIME:
                // The format is "YYYY:MM:DD HH:MM:SS" with time shown in 24-hour format
                SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(attr)
            }
        } catch (e: Exception) {
            null
        }

fun Bitmap.applyMatrix(matrix: Matrix?): Bitmap =
        if (matrix == null) this
        else Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)

fun decodeImageFileEx(path: String, maxSize: Int = 0, canvasHandler: (Canvas.() -> Unit)? = null): Bitmap? =
        decodeFileInternal({ BitmapFactory.decodeFile(path, it) }, { ExifInterface(path) }, maxSize, canvasHandler)

fun decodeImageFileDescriptorEx(fd: FileDescriptor, maxSize: Int = 0, canvasHandler: (Canvas.() -> Unit)? = null): Bitmap? =
        decodeFileInternal({ BitmapFactory.decodeFileDescriptor(fd, null, it) }, { ExifInterface(fd) }, maxSize, canvasHandler)

private fun decodeFileInternal(bfFun: (BitmapFactory.Options) -> Bitmap?,
                               exifFun: () -> ExifInterface,
                               maxSize: Int, canvasHandler: (Canvas.() -> Unit)?): Bitmap? {
    val opts = BitmapFactory.Options()
    if (maxSize > 0) {
        opts.inJustDecodeBounds = true
        bfFun(opts)
        val size = minOf(opts.outWidth, opts.outHeight)
        if (size <= 0) return null
        opts.inSampleSize = maxOf((size.toFloat() / maxSize.toFloat()).roundToInt(), 1)
        opts.inJustDecodeBounds = false
    }
    if (canvasHandler != null) {
        opts.inMutable = true
    }
    val matrix = exifFun().getRotationMatrix()
    return bfFun(opts)?.applyMatrix(matrix)?.also {
        if (it.isMutable) canvasHandler?.invoke(Canvas(it))
    }
}