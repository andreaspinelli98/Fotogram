package com.example.fotogram.model

import android.util.Log
import com.example.fotogram.Location
import com.example.fotogram.Post
import com.example.fotogram.User
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.emptyList

class Repository(private val serverCalls: ServerCalls,
                 private val settingsRepository: SettingsRepository ) {
    //Avvio dell'app
    suspend fun inizio(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val isFirst = settingsRepository.getPrimaVolta()
            val sid = settingsRepository.getSid()
            val uid = settingsRepository.getUid()
            //Log.d("primaVolta", "Valore: $isFirst")

            if (!isFirst) { //se non è il primo avvio, non serve rifare nulla
                Log.d("primaVolta", "Già inizializzato! Carico uid: $uid e sid: $sid")
                return@withContext Result.success(Unit)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("Errore in inizio: ${e.message}")
            Result.failure(e)
        }
    }

    //Crea un nuovo utente
    suspend fun registraUtente() {
        try {
           val dati = serverCalls.postUser()
           settingsRepository.setUid(dati.userId)
           settingsRepository.setSid(dati.sessionId)

           Log.d("Repository", "Utente registrato: uid=${dati.userId}, " +
                   "sid=${dati.sessionId}"
           )
        } catch (e: Exception) {
            println("Errore in registraUtente: ${e.message}")
        }
    }

    //Aggiorna dati dell'utente
    suspend fun aggiornaUtente(nome: String, bio: String?, dataNascita: String?) {
        try {
            val sid = settingsRepository.getSid()
            val body = UpdateUser(
                username = nome,
                bio = bio,
                dateOfBirth = dataNascita
            )

            serverCalls.putUser(sid, body)
            Log.d("Repository", "Dati utente aggiornati")
        } catch (e: Exception) {
            println("Errore in aggiornaUtente: ${e.message}")
        }
    }

    //Aggiorna foto profilo dell'utente
    suspend fun aggiornaFotoProfilo(fotoProfilo: String?) {
        try {
            val sid = settingsRepository.getSid()
            val body = UpdatePictureUser(
                base64 = fotoProfilo.toString()
            )

            serverCalls.putUserImage(sid, body)
            Log.d("Repository", "Immagine profilo utente aggiornata")
        } catch (e: Exception) {
            println("Errore in aggiornaProfilo: ${e.message}")
        }
    }

    //Mostrare feed
    suspend fun ottieniFeed(maxPostId: Int? = null): List<Int> {
        try {
            val sid = settingsRepository.getSid()
            val dati = serverCalls.getFeed(sid, maxPostId)

            Log.d("Repository", "Feed ottenuto: $dati") //Feed ottenuto: [28, 25, 22, 19, 17, 16, 15, 13, 10, 7]
            return dati
        } catch (e: Exception) {
            println("Errore in ottieniFeed: ${e.message}")
            return emptyList()
        }
    }

    suspend fun ottieniPost(postId: Int, cacheLocalePost: MutableList<Post>): Post? {
        try {
            /*val postInCache = cacheLocalePost.find { it.id == postId }

            if (postInCache != null) {
                Log.d("Repository", "Post $postId dalla cache")
                return postInCache
            }*/

            //se non c'è
            val sid = settingsRepository.getSid()
            val postScaricato = serverCalls.getPost(sid, postId)
            Log.d("Repository", "Post ottenuto: $postScaricato")

            cacheLocalePost.add(postScaricato)
            settingsRepository.setPosts(cacheLocalePost.toList()) //salva tutto

            return postScaricato
        } catch (e: Exception) {
            println("Errore in ottieniPost: ${e.message}")
            return null
        }
    }

    suspend fun ottieniUtente(authorId: Int, cacheLocaleAuthor: MutableList<User>): User? {
        try {
            /*val userInCache = cacheLocaleAuthor.find { it.id == authorId }

            if (userInCache != null) {
                Log.d("Repository", "Autore $authorId dalla cache")
                return userInCache
            }*/

            val sid = settingsRepository.getSid()
            val userScaricato = serverCalls.getUser(sid, authorId)
            Log.d("Repository", "Utente ottenuto: $userScaricato")

            cacheLocaleAuthor.add(userScaricato)
            settingsRepository.setUsers(cacheLocaleAuthor.toList())

            return userScaricato
        } catch (e: Exception) {
            println("Errore in ottieniUtente: ${e.message}")
            return null
        }
    }

    //Ottenere i post
    suspend fun ottieniListaPost(id: Int, maxPostId: Int?): List<Int> {
        try {
            val sid = settingsRepository.getSid()
            val lista = serverCalls.getPostList(sid, id, maxPostId)

            Log.d("Repository", "Lista ottenuta: $lista")
            return lista
        } catch (e: Exception) {
            println("Errore in ottieniListaPost: ${e.message}")
            return emptyList()
        }
    }

    suspend fun leggiListaPost(id: Int): Post? {
        try {
            val sid = settingsRepository.getSid()
            val post = serverCalls.getPost(sid, id)

            Log.d("Repository", "Post ottenuto: $post")
            return post
        } catch (e: Exception) {
            println("Errore in ottieniListaPost: ${e.message}")
            return null
        }
    }

    //Cambiare following/unfollowing
    suspend fun cancellaFollowing(id: Int) {//id dell'utente che perde il follow
        try {
            val sid = settingsRepository.getSid()
            serverCalls.deleteFollowing(sid, id)

        } catch (e: Exception) {
            println("Errore in cancellaFollowing: ${e.message}")
        }
    }

    suspend fun aggiornaFollowing(id: Int) {//id dell'utente che riceve il follow
        try {
            val sid = settingsRepository.getSid()
            serverCalls.putFollowing(sid, id)

        } catch (e: Exception) {
            println("Errore in aggiornaFollowing: ${e.message}")
        }
    }

    //Ottenere dati utente
    suspend fun ottieniDatiUtente(): User?{
        try {
            val sid = settingsRepository.getSid()
            val uid = settingsRepository.getUid()
            val dati = serverCalls.getUser(sid, uid)

            return dati
        } catch (e: Exception) {
            println("Errore in aggiornaFollowing: ${e.message}")
            return null
        }
    }

    //Creare post
    suspend fun creaPost(messaggio: String, foto: String?, coords: Point?) {
        try {
            val sid = settingsRepository.getSid()
            val body = SendPost(
                contentText = messaggio,
                contentPicture = foto,
                location = coords?.let { //se coords è null, let non viene eseguito e restituisce null automaticamente
                    Location (
                        latitude = it.latitude(),
                        longitude = it.longitude()
                    )
                } ?: Location() //se null, manda Location(null, null)
            )

            serverCalls.postPost(sid, body)
            Log.d("Repository", "Post caricato")
        } catch (e: Exception) {
            println("Errore in caricaPost: ${e.message}")
        }
    }
}