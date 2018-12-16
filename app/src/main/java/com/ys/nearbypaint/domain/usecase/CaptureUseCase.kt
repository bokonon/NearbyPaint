package com.ys.nearbypaint.domain.usecase

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import com.ys.nearbypaint.utiles.NearbyPaintLog
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CaptureUseCase {

    companion object {
        private val TAG: String = if (javaClass.simpleName != null) javaClass.simpleName else "CaptureUseCase"
    }

    /**
     * Store Capture Data.
     * @return String fileName
     */
    fun captureCanvas(context: Context, bitmap: Bitmap) : String? {
        var filePath: String? = null
        // decide capture directory
        val dir: File
        val path = Environment.getExternalStorageDirectory().toString() + "/ys.NearbyPaint/"
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            dir = File(path)
            dir.mkdirs()
        } else {
            dir = Environment.getDataDirectory()
        }
        // make original file name
        val fileName = getFileName()
        filePath = dir.absolutePath + "/" + fileName

        val file = File(path + fileName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: FileNotFoundException) {
            NearbyPaintLog.e(TAG, e.message, e)
            return filePath
        } finally {
            bitmap.recycle()
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    NearbyPaintLog.e(TAG, e.message, e)
                }

            }
        }
        // update gallery
        val values = ContentValues()
        val contentResolver = context.contentResolver
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put("_data", filePath)
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        NearbyPaintLog.d(TAG, "FilePath : $filePath")

        return filePath
    }

    /**
     * make original file name
     * @return String
     */
    private fun getFileName(): String {
        val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        val date = Date()
        return "NearbyPaint_" + formatter.format(date) + ".png"
    }
}