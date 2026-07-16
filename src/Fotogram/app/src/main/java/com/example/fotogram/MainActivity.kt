package com.example.fotogram

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.preferencesDataStore
import com.example.fotogram.model.Repository
import com.example.fotogram.model.ServerCalls
import com.example.fotogram.model.SettingsRepository
import com.example.fotogram.ui.components.FeedScreen
import com.example.fotogram.ui.theme.FotogramTheme
import com.example.fotogram.viewmodel.AppViewModel
import com.example.fotogram.viewmodel.FotogramViewModel
import com.example.fotogram.viewmodel.Screen

private val Context.settingsDataStore by preferencesDataStore(name = "settings")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsDataStore = applicationContext.settingsDataStore
        val settingsRepository = SettingsRepository(settingsDataStore)
        val serverCalls = ServerCalls()

        val appViewModel = AppViewModel(settingsRepository)
        val repository = Repository(
            serverCalls = serverCalls,
            settingsRepository = settingsRepository
        )
        val fotogramViewModel = FotogramViewModel(repository, settingsRepository)

        enableEdgeToEdge()
        setContent {
            FotogramTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Home(
                        appViewModel = appViewModel,
                        repository = repository,
                        fotogramViewModel = fotogramViewModel,
                        settingsRepository = settingsRepository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) //serve per usare TopAppBar
@Composable
fun Home(
    appViewModel: AppViewModel, repository: Repository, fotogramViewModel: FotogramViewModel,
    settingsRepository: SettingsRepository, modifier: Modifier = Modifier
) {
    val primaVolta = appViewModel.primaVolta
    var schermata by remember { mutableStateOf("Bacheca") }

    LaunchedEffect(primaVolta) {
        when (primaVolta) {
            true -> schermata = "Registrazione"
            false -> schermata = "Bacheca"
            null -> {}
        }
    }

    LaunchedEffect(Unit) {
        repository.inizio()
    }

    //Schermate che mostrano la bottom bar
    val schermatePrincipali = listOf( "Bacheca", "CreaPost", "Profilo")
    val mostraBar = schermata in schermatePrincipali && fotogramViewModel.screen == Screen.MASTER
    val isSchermataSecondaria = schermata != "Registrazione" && schermata != "Bacheca" && schermata !in schermatePrincipali ||
            (schermata == "Bacheca" && fotogramViewModel.screen != Screen.MASTER)

    Scaffold(
        topBar = @Composable {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            schermata == "Bacheca" && fotogramViewModel.screen == Screen.DETAIL_PROFILE -> "Dettagli utente"
                            schermata == "Bacheca" && fotogramViewModel.screen == Screen.DETAIL_IMAGE -> "Immagine"
                            schermata == "Bacheca" -> "Bacheca"
                            schermata == "CreaPost" -> "Crea Post"
                            schermata == "Profilo" -> "Profilo"
                            schermata == "Mappa" -> "Mappa"
                            else -> ""
                        }
                    )
                },
                navigationIcon = {
                    if (isSchermataSecondaria) {
                        IconButton(onClick = {
                            when {
                                schermata == "Bacheca" && fotogramViewModel.screen == Screen.DETAIL_PROFILE -> {
                                    fotogramViewModel.reset()
                                    fotogramViewModel.navigateTo(Screen.MASTER)
                                }
                                schermata == "Bacheca" && fotogramViewModel.screen == Screen.DETAIL_IMAGE -> {
                                    Log.d("ImmagineGrande", "Torno indietro, post: ${fotogramViewModel.selectedPost?.id}")
                                    val destination = fotogramViewModel.previousScreen.value
                                    fotogramViewModel.navigateTo(destination)
                                }
                                schermata == "Mappa" -> {
                                    if (fotogramViewModel.selectedPostLocation != null) {
                                        fotogramViewModel.pulisciPosizionePost()
                                        schermata = "Bacheca"
                                    } else {
                                        schermata = "CreaPost"
                                    }
                                }
                                else -> schermata = "Bacheca"
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                }
            )
        },

        bottomBar = {
            if (mostraBar) {
                NavigationBar{
                    NavigationBarItem(
                        selected = schermata == "Bacheca",
                        onClick = { schermata = "Bacheca" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Bacheca") },
                        label = { Text("Bacheca") }
                    )
                    NavigationBarItem(
                        selected = schermata == "CreaPost",
                        onClick = { schermata = "CreaPost" },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Crea Post") },
                        label = { Text("Crea Post") }
                    )
                    NavigationBarItem(
                        selected = schermata == "Profilo",
                        onClick = { schermata = "Profilo" },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profilo") },
                        label = { Text("Profilo") }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (schermata) {
            "Registrazione" -> Registrazione(
                modifier = Modifier.padding(innerPadding),
                appViewModel = appViewModel,
                onBack = { schermata = "Bacheca" }
            )

            "Bacheca" -> {
                when (fotogramViewModel.screen) {
                    Screen.MASTER -> FeedScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = fotogramViewModel,
                        repository = repository,
                        settingsRepository = settingsRepository,
                        onPostClick = { post ->
                            fotogramViewModel.navigateTo(Screen.DETAIL_IMAGE, newSelectedPost = post)
                        },
                        onAuthorClick = { user ->
                            fotogramViewModel.navigateTo(Screen.DETAIL_PROFILE, newSelectedAuthor = user)
                        },
                        onNavigateToMappa = { schermata = "Mappa" },
                        onSetPosizionePost = { location -> fotogramViewModel.setPosizionePost(location) }
                    )

                    Screen.DETAIL_PROFILE -> DettagliAmico(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = fotogramViewModel,
                        onPostClick = { post ->
                            fotogramViewModel.navigateTo(
                                Screen.DETAIL_IMAGE,
                                Screen.DETAIL_PROFILE,
                                newSelectedPost = post
                            )
                        },
                        onBackClick = {
                            fotogramViewModel.reset()
                            fotogramViewModel.navigateTo(Screen.MASTER)
                        }
                    )

                    Screen.DETAIL_IMAGE -> ImmagineGrande(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = fotogramViewModel,
                        onBackClick = {
                            val destination = fotogramViewModel.previousScreen.value
                            fotogramViewModel.navigateTo(destination)
                        }
                    )
                }
            }

            "Profilo" -> Profilo(
                modifier = Modifier.padding(innerPadding),
                viewModel = fotogramViewModel,
                onPostClick = { post ->
                    fotogramViewModel.navigateTo(
                        Screen.DETAIL_PROFILE,
                        Screen.DETAIL_PROFILE,
                        newSelectedPost = post
                    )
                    Log.d("MainActivity", "Bottone 'Profilo' premuto")
                },
                onBack = { Log.d("MainActivity", "Torno su Bacheca")
                    schermata = "Bacheca" }
            )

            "CreaPost" -> CreaPost(
                modifier = Modifier.padding(innerPadding),
                viewModel = fotogramViewModel,
                onBack = { Log.d("MainActivity", "Torno su Bacheca")
                    schermata = "Bacheca" },
                onNavigateToMappa = { schermata = "Mappa" }
            )

            "Mappa" -> Mappa(
                modifier = Modifier.padding(innerPadding),
                appViewModel = appViewModel,
                viewModel = fotogramViewModel,
                onBack = {
                    if (fotogramViewModel.selectedPostLocation != null) {
                        fotogramViewModel.pulisciPosizionePost()
                        Log.d("MainActivity", "Torno su Bacheca")
                        schermata = "Bacheca"
                    } else {
                        Log.d("MainActivity", "Torno su Crea Post")
                        schermata = "CreaPost"
                    }
                }
            )

            "DettagliAmico" -> DettagliAmico(
                modifier = Modifier.padding(innerPadding),
                viewModel = fotogramViewModel,
                onPostClick = { schermata = "ImmagineGrande" },
                onBackClick = { Log.d("MainActivity", "Torno su Bacheca")
                    schermata = "Bacheca" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomePreview() {
    FotogramTheme { //passo dati finti perché Preview non ha contesto Android
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Bacheca") })
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Bacheca") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        label = { Text("Crea Post") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profilo") }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                Text("Contenuto bacheca...", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}