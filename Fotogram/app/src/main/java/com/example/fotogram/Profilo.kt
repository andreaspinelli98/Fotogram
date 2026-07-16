package com.example.fotogram

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fotogram.ui.components.Base64ImageLoader
import com.example.fotogram.viewmodel.FotogramViewModel
import com.example.fotogram.ui.components.AddImage
import com.example.fotogram.ui.components.StatItem
import com.example.fotogram.ui.components.showPosts

//import data
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.draw.clip
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun Profilo (modifier: Modifier, viewModel: FotogramViewModel, onPostClick: (Post) -> Unit,
             onBack: () -> Unit) {
    val dati by remember { derivedStateOf { viewModel.selectedAuthor } }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val lista by viewModel.listaPost

    var fotoProfilo by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }

    var dataNascita by remember { mutableStateOf("") }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        //tutte le date dopo oggi sono disabilitate (non cliccabili)
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.ottieniDatiUtente()
    }

    LaunchedEffect(dati) { //dati è aggiornato ogni volta che cambia (dopo la chiamata asincrona)
        if (dati == null) return@LaunchedEffect //se dati è null, esce dal LaunchedEffect senza eseguire il resto
        dati?.let {
            username = it.username ?: ""
            bio = it.bio ?: ""
            fotoProfilo = it.profilePicture ?: ""
            dataNascita = it.dateOfBirth ?: ""
        }
        //se dati non è null, prendi id; se anche id non è null, esegui il blocco
        dati?.id?.let { viewModel.ottieniLista(it) }
    }

    if (dati == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn (
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp)
    ){
        //Foto profilo
        item {
            Box(contentAlignment = Alignment.TopEnd) {
                Base64ImageLoader(
                    modifier = Modifier.size(100.dp).clip(CircleShape).clickable { showImagePicker = true },
                    fotoProfilo.ifEmpty { dati!!.profilePicture ?: "" }
                )
                //icona matita sopra la foto
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambia foto",
                    modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp)
                )
            }

            if (showImagePicker) {
                AddImage(onImageSelected = { base64 ->
                    fotoProfilo = base64
                    showImagePicker = false
                })
            }
        }

        //Statistiche follower/following/post
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Follower", value = "${dati!!.followersCount}")
                StatItem(label = "Following", value = "${dati!!.followingCount}")
                StatItem(label = "Post", value = "${dati!!.postsCount}")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        //Campi modificabili
        item {
            TextField(
                value = username,
                onValueChange = { if (it.length <= 15) username = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(0.9f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            TextField(
                value = bio,
                onValueChange = { if (it.length <= 100) bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(0.9f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        //Data di nascita
        item {
            TextField(
                value = dataNascita,
                onValueChange = {},
                label = { Text("Data di nascita") },
                placeholder = { Text("yyyy-mm-dd") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleziona data")
                    }
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(onClick = {
                            val millis = datePickerState.selectedDateMillis
                            if (millis != null) {
                                val localDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                dataNascita = localDate.format(formatter)
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        Button(onClick = { showDatePicker = false }) { Text("Annulla") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        //Bottone Salva
        item {
            Button(
                onClick = {
                    viewModel.aggiornaProfilo(username, bio, dataNascita, fotoProfilo)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text("Salva modifiche")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        //Lista post
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "Post pubblicati", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 8.dp)
            )
        }

        showPosts(lista, dati, onPostClick)
    }
}