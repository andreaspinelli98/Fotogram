package com.example.fotogram

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fotogram.ui.components.AddImage
import com.example.fotogram.ui.components.Base64ImageLoader
import com.example.fotogram.viewmodel.FotogramViewModel
import androidx.compose.runtime.collectAsState

@Composable //se da creaPost cambio schermata, il messaggio viene salvato
fun CreaPost (modifier: Modifier, viewModel: FotogramViewModel, onBack: () -> Unit,
              onNavigateToMappa: () -> Unit) {

    var alertImmagine by remember { mutableStateOf(false) }
    var alertPost by remember { mutableStateOf(false) }
    val foto by viewModel.foto.collectAsState()
    val messaggio by viewModel.messaggio.collectAsState()
    val point by viewModel.selectedPoint.collectAsState()

    if (alertImmagine) {    //se immagine troppo grande
        AlertDialog(
            onDismissRequest = { alertImmagine = false },
            title = { Text("Immagine troppo grande") },
            text = { Text("Inserisci un'altra immagine") },
            confirmButton = {
                TextButton(onClick = { alertImmagine = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (alertPost) {
        AlertDialog(
            onDismissRequest = { alertPost = false },
            title = { Text("Contenuto mancante") },
            text = { Text("Aggiungi un messaggio ed un'immagine") },
            confirmButton = {
                TextButton(onClick = { alertPost = false }) {
                    Text("OK")
                }
            }
        )
    }

    LazyColumn (modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
    ) {
        //Immagine
        item {
            AddImage { base64 ->
                if (base64.length > 80_000) {
                    alertImmagine = true
                    viewModel.setFoto(base64)
                } else {
                    viewModel.setFoto(base64)
                }
            }
        }

        foto?.let { base64 -> //anteprima
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box {
                    Base64ImageLoader(modifier = Modifier.fillMaxWidth()
                            .heightIn(max = 300.dp), //altezza massima per non coprire tutto
                        base64
                    )

                    IconButton(//bottone X per rimuovere la foto
                        onClick = { viewModel.setFoto(null) },
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Rimuovi immagine")
                    }
                }
            }
        }

        //Campo testo
        item {
            Spacer(modifier = Modifier.height(16.dp))
            TextField(value = messaggio, onValueChange = { if (it.length <= 100) viewModel.setMessaggio(it) },
                label = { Text("Messaggio") }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), //altezza minima visibile
                placeholder = { Text("messaggio") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default    //"a capo" invece di "invio"
                ),
                supportingText = { Text("${messaggio.length}/100") } //contatore caratteri
            )
        }

        //Posizione
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                    onNavigateToMappa()
                    Log.d("CreaPost", "Vado su mappa")
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (point != null) {
                    "Posizione aggiunta"
                } else {
                    "Aggiungi posizione"
                })
            }
        }

        //Bottone carica
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (foto == null || messaggio.isBlank()) {
                    alertPost = true
                } else {
                    viewModel.creaPost(messaggio, foto, point)
                    viewModel.pulisciPost()
                    onBack()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Log.d("creaPost", "Post caricato" )
                Text("Carica Post")
            }
        }
    }
}