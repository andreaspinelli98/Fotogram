package com.example.fotogram

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fotogram.ui.components.Base64ImageLoader
import com.example.fotogram.viewmodel.FotogramViewModel

@Composable
fun ImmagineGrande (modifier: Modifier = Modifier, viewModel: FotogramViewModel,
    onBackClick: () -> Unit
) {
    val post = viewModel.selectedPost
    Box(modifier = Modifier.fillMaxSize()) {
        post?.let { Base64ImageLoader(modifier = Modifier.fillMaxSize(),
            it.contentPicture)
        }
    }
}
