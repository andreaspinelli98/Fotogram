package com.example.fotogram.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fotogram.Post
import com.example.fotogram.User

fun LazyListScope.showPosts(lista: List<Post?>, author: User?, onPostClick: (Post) -> Unit ) {
    if (lista.isEmpty()) {
        item {
            Text(text = "Nessun post pubblicato", modifier = Modifier.padding(16.dp))
        }
    } else {
        items(lista) { post ->
            FeedItem(post = post, author = author, onPostClick = { post?.let { onPostClick(it) } })
        }
    }
}