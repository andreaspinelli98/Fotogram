package com.example.fotogram.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fotogram.Post
import com.example.fotogram.User
import com.example.fotogram.model.Repository
import com.example.fotogram.viewmodel.FotogramViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.collections.get
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.example.fotogram.Location
import com.example.fotogram.model.SettingsRepository

@Composable
fun FeedScreen(modifier: Modifier, viewModel: FotogramViewModel, repository: Repository, settingsRepository: SettingsRepository,
               onPostClick: (Post) -> Unit, onAuthorClick: (User) -> Unit,
               onNavigateToMappa: () -> Unit, onSetPosizionePost: (Location) -> Unit) {

    LaunchedEffect(Unit) {
        viewModel.caricaFeed()
    }

    val feed = viewModel.feed

    if (feed.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val lazyColState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = viewModel.firstVisibleItemScrollOffset
    )

    //evita che ad ogni micro-scorrimento, ci siano troppi refresh della schermata
    val currentIndex = remember { derivedStateOf { lazyColState.firstVisibleItemIndex } }
    val currentOffset = remember { derivedStateOf { lazyColState.firstVisibleItemScrollOffset } }
    val lastVisibleIndex = remember {
        derivedStateOf { lazyColState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }

    //traccia la posizione dello scroll per il ripristino
    LaunchedEffect(currentIndex.value, currentOffset.value) {
        Log.d("FeedScreen", "${currentIndex.value}")
        viewModel.saveListScrollState(
            lazyColState.firstVisibleItemIndex,
            lazyColState.firstVisibleItemScrollOffset
        )
    }

    //attiva la fetch quando vicino al fondo
    LaunchedEffect(lastVisibleIndex.value) {
        val lastVisibleIndex = lazyColState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val total = lazyColState.layoutInfo.totalItemsCount
        val threshold = total - 3 //quanti post mancano a fine della lista
        if (lastVisibleIndex != null && lastVisibleIndex >= threshold) {
            viewModel.caricaPaginaSuccessiva()
        }
    }

    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = viewModel.isRefreshing, //controlla se sto ricaricando (mostra indicatore)
        onRefresh = { viewModel.refreshList() } //che cosa deve fare quando sto ricaricando
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyColState,
        ) {
            /*item {
                Text(text = "Lista dei Post", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }*/

            items(feed) { postId ->
                val post = viewModel.posts[postId]
                val author = viewModel.authors[post?.authorId]

                FeedItem(post = post, author = author,
                    onPostClick = { post?.let { onPostClick(it) } },
                    onAuthorClick = { author?.let { onAuthorClick(it) } },
                    onNavigateToMappa = { post?.location?.let { location ->
                            onSetPosizionePost(location)  //usa il callback esterno
                        }
                        onNavigateToMappa()
                    }
                )
            }
        }
    }
}

@Composable
fun FeedItem(post: Post?, author: User?, onPostClick: () -> Unit,
    onAuthorClick: () -> Unit = {},
    onNavigateToMappa: () -> Unit = {}
) {
    if (author == null || post == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val sfondo = if (author.isYourFollowing) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(15.dp).background(sfondo)
            .border(width = 1.dp, Color.Black) //Color(0xFFE1E8ED)
    ) {
        Text("Autore: ${author.username}")
        Base64ImageLoader(modifier = Modifier.clickable {
            Log.d("FeedScreen", "Profilo di ${author.id} premuto")
            onAuthorClick()},
            author.profilePicture
        )

        Text("Testo: ${post.contentText}")
        Base64ImageLoader(modifier = Modifier.clickable {
            Log.d("FeedScreen", "Post ${post.id} premuto")
            onPostClick()},
            post.contentPicture
        )

        post.location?.let {
            if (it.isValid() ) {
                Button(onClick = {
                    onNavigateToMappa()
                    Log.d("FeedScreen", "Vado su mappa")
                }) {
                    Text(text = "Vedi su mappa")
                }
            } else {
                Text(text = "Posizione non condivisa")
            }
        }
    }
}