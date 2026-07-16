package com.example.fotogram

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.fotogram.ui.components.Base64ImageLoader
import com.example.fotogram.ui.components.showPosts
import com.example.fotogram.ui.components.StatItem
import com.example.fotogram.viewmodel.FotogramViewModel

@Composable
fun DettagliAmico (modifier: Modifier, viewModel: FotogramViewModel, onPostClick: (Post) -> Unit,
                   onBackClick: () -> Unit) {
    //isYourFollower è un valore calcolato dal server
    val dati = viewModel.selectedAuthor
    val lista: List<Post?> by viewModel.listaPost //a differenza di uguale, il by include l'uso di .value
    val following = viewModel.following

    LaunchedEffect(dati) { //eseguito ogni volta che dati cambia
        dati?.let { viewModel.following = it.isYourFollowing }
    }

    LaunchedEffect(Unit) {
        //se dati non è null, prendi id; se anche id non è null, esegui il blocco
        dati?.id?.let { viewModel.ottieniLista(it) }
    }

    if (lista.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn (
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 20.dp)
    ) {
        //Foto e info utente
        item {
            dati?.let {
                Base64ImageLoader(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    it.profilePicture
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = dati?.username ?: "", style = MaterialTheme.typography.titleLarge)
            Text(text = dati?.bio ?: "", style = MaterialTheme.typography.bodyMedium)
            Text(text = dati?.dateOfBirth ?: "", style = MaterialTheme.typography.bodySmall)
        }

        //Statistiche in riga
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Follower", value = "${dati?.followersCount}")
                StatItem(label = "Following", value = "${dati?.followingCount}")
                StatItem(label = "Post", value = "${dati?.postsCount}")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        //Bottone segui/smetti
        item {
            Button (
                onClick = { viewModel.amicizia(dati?.id) },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(if (following) {
                    "Smetti di seguire"
                } else {
                    "Inizia a seguire"
                })
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
        }

        //Lista post
        showPosts(lista, dati, onPostClick)
    }
}