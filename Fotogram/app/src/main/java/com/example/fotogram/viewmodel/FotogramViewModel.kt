package com.example.fotogram.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fotogram.Location
import com.example.fotogram.Post
import com.example.fotogram.User
import com.example.fotogram.model.Repository
import com.example.fotogram.model.SettingsRepository
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

enum class Screen {
    MASTER,
    DETAIL_IMAGE,
    DETAIL_PROFILE,
}

class FotogramViewModel(private val repository: Repository,
                        private val settingsRepository: SettingsRepository) : ViewModel() {
    var feed by mutableStateOf<List<Int>>(emptyList())
        private set
    private var lastPostId: Int? = null
    var posts by mutableStateOf<Map<Int, Post>>(emptyMap())
        private set
    var authors by mutableStateOf<Map<Int, User>>(emptyMap())
        private set

    //App state
    var screen by mutableStateOf(Screen.MASTER)
        private set
    var selectedPost by mutableStateOf<Post?>(null)
        private set
    var selectedAuthor by mutableStateOf<User?>(null)
        private set

    //List state
    var isLoading by mutableStateOf(false)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    //Scroll state
    var firstVisibleItemIndex by mutableIntStateOf(0) //primo elemento visibile (es 28)
        private set
    var firstVisibleItemScrollOffset by mutableIntStateOf(0) //a che punto sono arrivato dell'elemento 28 (es ¾ o inizio)
        private set

    private var feedInizializzato = false //flag per separare primo caricamento da caricamento pagine successive

    private suspend fun eseguiCaricamentoFeed() {
        try {
            isLoading = true

            val feedIds: List<Int> = repository.ottieniFeed(maxPostId = lastPostId)
            lastPostId = feedIds.last() - 1

            //prima tutti i post, poi tutti gli autori
            val cacheLocalePost = settingsRepository.getPosts().toMutableList() //legge una volta sola
            val postsList = feedIds.map { id: Int ->
                repository.ottieniPost(id, cacheLocalePost)
            }

            val cacheLocaleAuthor = settingsRepository.getUsers().toMutableList()
            val authorsMap = postsList
                .filterNotNull()
                .map { post -> post.authorId }
                .distinct()
                .map { authorId: Int ->
                    authorId to repository.ottieniUtente(authorId, cacheLocaleAuthor)
                }
                .filter { (_, user) -> user != null }
                .associate { (id, user) -> id to user!! }

            isLoading = false
            feed = feed + postsList.filterNotNull().map { it.id }
            posts = posts + postsList.filterNotNull().associateBy { it.id }
            authors = authors + authorsMap
            feedInizializzato = true
        } catch (e: Exception) {
            Log.e("FotogramViewModel", "Errore nel caricamento feed: $e")
            isLoading = false
        }
    }

    //Eseguita con avvio dell'app e refresh esplicito
    fun caricaFeed() {
        if (isLoading) return //blocca chiamate multiple
        if (feedInizializzato) return
        viewModelScope.launch {
            eseguiCaricamentoFeed()
        }
    }

    //Eseguita per scroll infinito
    fun caricaPaginaSuccessiva() {
        if (isLoading) return
        viewModelScope.launch {
            eseguiCaricamentoFeed()
        }
    }

    //Navigo da una schermata all’altra
    var previousScreen = mutableStateOf(Screen.MASTER)
    fun navigateTo(newScreen: Screen, fromScreen: Screen? = null, newSelectedPost: Post? = null,
                   newSelectedAuthor: User? = null) {
        if (fromScreen != null) {
            previousScreen.value = fromScreen
        }
        screen = newScreen
        newSelectedPost?.let { selectedPost = it }  //aggiorna solo se passato
        newSelectedAuthor?.let { selectedAuthor = it }
        Log.d("navigazione", "Vado su: $newScreen, da: $fromScreen, previousScreen: $previousScreen")
    }

    fun refreshList() {
        if (isRefreshing) return
        feedInizializzato = false
        lastPostId = null  //reset per ricaricare dall'inizio
        feed = emptyList()

        viewModelScope.launch {
            isRefreshing = true
            posts = emptyMap()
            eseguiCaricamentoFeed()
            isRefreshing = false
        }
        Log.d("FotogramViewModel", "Lista Ripulita")
    }

    fun saveListScrollState(index: Int, offset: Int) {
        firstVisibleItemIndex = index
        firstVisibleItemScrollOffset = offset
    }

    //Eseguita per ottenere i post di un solo autore
    val listaPost = mutableStateOf<List<Post?>>(emptyList())
    private var maxPostId: Int? = null
    private var isLastPage = false

    //Eseguita ogni volta che entro nel profilo di un utente
    fun reset() {
        listaPost.value = emptyList()
        maxPostId = Int.MAX_VALUE
        isLastPage = false
    }

    fun ottieniLista(id: Int) {
        if (isLastPage) return

        viewModelScope.launch {
            val lista = repository.ottieniListaPost(id, maxPostId)

            if (lista.isEmpty()) {
                isLastPage = true
                return@launch
            }

            maxPostId = lista.last() - 1
            leggiLista(lista)
        }
    }

    suspend fun leggiLista(lista: List<Int>) {
        val ris = mutableListOf<Post?>()

        for (id in lista) {
            val post = repository.leggiListaPost(id)
            ris.add(post)
        }
        listaPost.value = ris
    }

    //Eseguito per dare/togliere follow
    var following by mutableStateOf(false)
    fun amicizia(id: Int?) {
       id ?: return //se id è null, esco dalla funzione
       viewModelScope.launch {
           if (following) {
               repository.cancellaFollowing(id)
           } else {
               repository.aggiornaFollowing(id)
           }
           following =! following //inverte il valore booleano: true diventa false, false diventa true
           selectedAuthor = selectedAuthor?.copy(isYourFollowing = following)
       }
    }

    //Eseguito per ottenere dati dell'utente
    fun ottieniDatiUtente() {
        viewModelScope.launch {
            selectedAuthor = repository.ottieniDatiUtente()
        }
    }

    //Eseguito per aggiornare il profilo dell'utente
    fun aggiornaProfilo(username: String, bio: String?, dataNascita: String?, foto: String) {
        viewModelScope.launch {
           repository.aggiornaUtente(username, bio, dataNascita)
           repository.aggiornaFotoProfilo(foto)
        }
    }

    //Eseguito per salvare messaggi e foto mentre scelgo la posizione da aggiungere
    private val _messaggio = MutableStateFlow("")
    var messaggio: StateFlow<String> = _messaggio

    fun setMessaggio(testo: String) {
        _messaggio.value = testo
    }

    private val _foto = MutableStateFlow<String?>(null)
    var foto: StateFlow<String?> = _foto

    fun setFoto(base64: String?) {
        _foto.value = base64
    }

    //Eseguito per gestire posizione del post
    var selectedPostLocation by mutableStateOf<Location?>(null)
        private set

    fun setPosizionePost(location: Location?) {
        selectedPostLocation = location
    }

    fun pulisciPosizionePost() {
        selectedPostLocation = null
    }

    fun rimuoviPunto() {
        _selectedPoint.value = null
    }

    //Eseguito per scegliere posizione da associare al post
    private val _selectedPoint = MutableStateFlow<Point?>(null)
    val selectedPoint: StateFlow<Point?> = _selectedPoint

    fun setSelectedPoint(point: Point) {
        _selectedPoint.value = point
    }

    //Svuotare il post dopo la pubblicazione
    fun pulisciPost() {
        _messaggio.value = ""
        _foto.value = null
        _selectedPoint.value = null
    }

    //Eseguito per creare post
    fun creaPost(messaggio: String, foto: String?, coords: Point?) {
        viewModelScope.launch {
            repository.creaPost(messaggio, foto, coords)
        }
    }
}
