package com.example.fotogram.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

//decodifica l'immagine in base64
@Composable
fun Base64ImageLoader(modifier: Modifier, base64String: String?) {
    if (base64String.isNullOrEmpty()) return
    //Log.d("Base64ImageLoader", "Stringa: ${base64String.take(50)}")  //mostra i primi 50 caratteri

    //remember con base64String come key ricalcola solo se la stringa cambia, non ad ogni recompose
    val bitmap = remember(base64String) {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    if (bitmap == null) return  //se la decodifica fallisce (stringa corrotta), evita crash

    Image(
        modifier = modifier,
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Immagine caricata da Base64",
        contentScale = ContentScale.Fit
    )
}