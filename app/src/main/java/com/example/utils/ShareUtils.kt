package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun shareBitmap(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val stream = FileOutputStream("$cachePath/story_export.png")
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val imagePath = File(context.cacheDir, "images")
        val newFile = File(imagePath, "story_export.png")
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            newFile
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, context.contentResolver.getType(contentUri))
            putExtra(Intent.EXTRA_STREAM, contentUri)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share your life progress"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
