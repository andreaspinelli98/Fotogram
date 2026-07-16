package com.example.fotogram

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fotogram.ui.components.Button
import com.example.fotogram.ui.components.AddImage
import com.example.fotogram.ui.components.Base64ImageLoader
import com.example.fotogram.viewmodel.AppViewModel

@Composable
fun Registrazione (modifier: Modifier, appViewModel: AppViewModel, onBack: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var fotoProfilo by remember { mutableStateOf<String?>(null) }
    var alertImmagine by remember { mutableStateOf(false) }
    var registrazioneInCorso by remember { mutableStateOf(false) }

    //se immagine troppo grande
    if (alertImmagine) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Registrazione", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = nome,
                onValueChange = { if (it.length <= 15) nome = it },
                label = { Text("Nome") },
                placeholder = { Text("Nome") }, //appare dentro il campo quando clicco sopra
                enabled = !registrazioneInCorso //disabilita i campi durante il caricamento
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Inserisci immagine")
            AddImage { base64 ->
                if (base64.length > 80_000) {
                    alertImmagine = true
                    fotoProfilo = null
                } else {
                    fotoProfilo = base64
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            fotoProfilo?.let {
                Base64ImageLoader(modifier, it)
            }

            Button(text = "Registrati", modifier = modifier, onClick = {
                    Log.d("MainActivity", "Bottone 'Registrati' premuto")
                    registrazioneInCorso = true //attiva lo spinner solo ora
                    appViewModel.eseguiRegistrazione(nome, fotoProfilo)
                }
            )

            //Spinner visibile solo dopo aver premuto il bottone
            if (registrazioneInCorso && !appViewModel.registrazioneCompletata) {
                CircularProgressIndicator()
            }

            if (appViewModel.registrazioneCompletata) {
                LaunchedEffect(Unit) {
                    onBack()
                }
            }
        }

        //Overlay semitrasparente bloccante sopra tutto
        if (registrazioneInCorso && !appViewModel.registrazioneCompletata) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {} //intercetta tutti i click
            )
        }
    }
}
