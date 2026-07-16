package com.example.fotogram.model

import android.util.Log
import com.example.fotogram.Post
import com.example.fotogram.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ServerCalls {
    private val urlString = "https://develop.ewlab.di.unimi.it/mc/2526"

    private val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }

    //Post per creare utente
    suspend fun postUser(): UserResponse {
        //https://develop.ewlab.di.unimi.it/mc/2526/user
        val url = "$urlString/user"
        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
        }.body()
    }

    //Put per aggiornare dati dell'utente
    suspend fun putUser(sid: String?, body: UpdateUser) {
        //https://develop.ewlab.di.unimi.it/mc/2526/user
        //Log.d("putUser", "BODY = $body")
        val url = "$urlString/user"
        val response = httpClient.put(url) {
            contentType(ContentType.Application.Json)
            header("x-session-id", sid)
            setBody(body)
        }

        val rawResponse = response.bodyAsText()
        Log.d("putUser", "response= $rawResponse")
    }

    //Put per aggiornare profilo dell'utente
    suspend fun putUserImage(sid: String?, body: UpdatePictureUser) {
        //https://develop.ewlab.di.unimi.it/mc/2526/user/image
        //Log.d("putUserImage", "BODY = $body")
        val url = "$urlString/user/image"
        val response = httpClient.put(url) {
            contentType(ContentType.Application.Json)
            header("x-session-id", sid)
            setBody(body)
        }

        val rawResponse = response.bodyAsText()
        Log.d("putUserImage", "response = $rawResponse")
    }

    //Get per ottenere il feed, i post e gli autori (utenti)
    suspend fun getFeed (sid: String?, maxPostId: Int? = null, limit: Int? = null, seed: Int? = null
    ): List<Int> {
        //https://develop.ewlab.di.unimi.it/mc/2526/feed?maxPostId=50&limit=10&seed=0
        val maxPostId = 30 //post fatti dal prof 50 in giù
        val url = "$urlString/feed"

        val response: List<Int> = httpClient.get(url) {
            contentType(ContentType.Application.Json)
            header("x-session-id", sid)

            //parametri
            parameter("maxPostId", maxPostId)
            limit?.let { parameter("limit", it) }
            seed?.let { parameter("seed", it) }
        }.body()

        Log.d("getFeed", "response = $response")
        return response
    }

    //Get per ottenere i post
    suspend fun getPost (sid: String?, postId: Int): Post {
        //https://develop.ewlab.di.unimi.it/mc/2526/post/1129
        val url = "$urlString/post/${postId}"
        val response: Post = httpClient.get(url) {
            contentType(ContentType.Application.Json)
            header("x-session-id", sid)
        }.body()

        Log.d("getPost", "response = $response")
        return response
    }

    //Get per ottenere l'autore/utente
    suspend fun getUser (sid: String?, authorId: Int?): User {
        //https://develop.ewlab.di.unimi.it/mc/2526/user/2374
        val url = "$urlString/user/${authorId}"
        val response: User = httpClient.get(url) {
            contentType(ContentType.Application.Json)
            header("x-session-id", sid)

        }.body()

        Log.d("getUser", "response = $response")
        return response
    }

    //Get per ottenere l'elenco dei psot di un autore
    suspend fun getPostList (sid: String?, id: Int?, maxPostId: Int? = null, limit: Int? = null):
            List<Int> {
        //https://develop.ewlab.di.unimi.it/mc/2526/post/list/6?maxPostId=50&limit=10
        val url = "$urlString/post/list/${id}"
        val response: List<Int> = httpClient.get(url) {
            contentType(ContentType.Application.Json)
            header("x-session-id", sid)

            //parametri
            maxPostId?.let { parameter("maxPostId", it) }
            limit?.let { parameter("limit", it) }
        }.body()

        Log.d("getPostList", "response = $response")
        return response
    }

    //Delete per cancellare following
    suspend fun deleteFollowing(sid: String?, id: Int) {
        val url = "$urlString/follow/${id}"
        //https://develop.ewlab.di.unimi.it/mc/2526/follow/6
        val response = httpClient.delete(url) {
            contentType(ContentType.Application.Json)
            header("x-session-id", sid)
        }

        Log.d("deleteFollowing", "$response")
    }

    //Put per aggiornare following
    suspend fun putFollowing(sid: String?, id: Int) {
        //https://develop.ewlab.di.unimi.it/mc/2526/follow/6
        val url = "$urlString/follow/${id}"
        val response = httpClient.put(url) {
            contentType(ContentType.Application.Json)
            header("x-session-id", sid)
            setBody(body)
        }

        Log.d("putFollowing", "$response")
    }

    //Post per caricare i post
    suspend fun postPost(sid: String?, body: SendPost) {
        //https://develop.ewlab.di.unimi.it/mc/2526/post
        //Log.d("postPost", "BODY = $body" )
        //Log.d("postPost", "Location = ${body.location}")
        val url = "$urlString/post"
        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("x-session-id", sid)
            setBody(body)
        }.body()
    }
}