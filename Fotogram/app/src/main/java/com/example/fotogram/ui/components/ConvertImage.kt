package com.example.fotogram.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

fun convertImageToBase64(context: Context, uri: Uri): String {
    //carica immagine dall'URI
    val stream = context.contentResolver.openInputStream(uri)
    var bitmap = BitmapFactory.decodeStream(stream)
    val out = ByteArrayOutputStream()
    stream?.close()

    //trasforma immagine in base64 con compressione
    val scale = minOf(600f / bitmap.width, 600f / bitmap.height)
    bitmap = bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt())
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
    return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
}
