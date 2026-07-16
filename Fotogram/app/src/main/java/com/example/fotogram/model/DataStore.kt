package com.example.fotogram.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fotogram.Post
import com.example.fotogram.User
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val PRIMAVOLTA = booleanPreferencesKey("primaVolta")
        val SESSION_ID = stringPreferencesKey("sid")
        val USER_ID = intPreferencesKey("uid")
        val POSTS =  stringPreferencesKey("posts")
        val AUTHORS = stringPreferencesKey("authors")
    }

    suspend fun setPrimaVolta(valore: Boolean) {
        dataStore.edit {
                preferences -> preferences[PRIMAVOLTA] = valore
        }
    }

    suspend fun getPrimaVolta() : Boolean {
        val prefs = dataStore.data.first()
        return prefs[PRIMAVOLTA] ?: true   //vero di default = primo avvio
    }

    suspend fun setSid(valore: String) {
        dataStore.edit { it[SESSION_ID] = valore }
    }

    suspend fun getSid(): String? {
        val prefs = dataStore.data.first()
        return prefs[SESSION_ID]
    }

    suspend fun setUid(valore: Int) {
        dataStore.edit { it[USER_ID] = valore }
    }

    suspend fun getUid(): Int? {
        val prefs = dataStore.data.first()
        return prefs[USER_ID]
    }

    suspend fun setPosts(posts: List<Post>) {
        val json = Json.encodeToString(posts) //converto una lista in JSON
        dataStore.edit {
            preferences -> preferences[POSTS] = json
        }
    }

    suspend fun getPosts(): List<Post> {
        val prefs = dataStore.data.first()

        val json = prefs[POSTS] ?: return emptyList()
        return Json.decodeFromString(json) //converto un JSON in lista
    }

    suspend fun setUsers(users: List<User>) {
        val json = Json.encodeToString(users)
        dataStore.edit { preferences -> preferences[AUTHORS] = json }
    }

    suspend fun getUsers(): List<User> {
        val prefs = dataStore.data.first()

        val json = prefs[AUTHORS] ?: return emptyList()
        return Json.decodeFromString(json)
    }
}