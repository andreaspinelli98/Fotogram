package com.example.fotogram.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fotogram.Location
import com.example.fotogram.model.Repository
import com.example.fotogram.model.ServerCalls
import com.example.fotogram.model.SettingsRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AppViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    var primaVolta by mutableStateOf<Boolean?>(null)
        private set

    init {
        primaVolta()
    }

    //Funzioni per controllare se è la prima volta
    fun salvaPrimaVolta() {
        viewModelScope.launch {
            val valore = primaVolta
            if (valore != null) {
                settingsRepository.setPrimaVolta(valore)
            }
        }
    }

    fun primaVolta() {
        viewModelScope.launch {
            primaVolta = settingsRepository.getPrimaVolta()
            Log.d("primaVolta", "Caricato: $primaVolta")
        }
    }

    fun completaPrimoAvvio() {
        //creata questa funz perchè primaVolta è private, e posso cambiarla solo qui, non nel main
        primaVolta = false
        salvaPrimaVolta()
    }

    //Funzione per eseguire registrazione
    private val repository = Repository (
        ServerCalls(),
        settingsRepository = settingsRepository
    )

    var registrazioneCompletata by mutableStateOf(false)
        private set

    fun eseguiRegistrazione(nome: String, fotoProfilo: String?) {
        viewModelScope.launch {
            try {
                repository.registraUtente()
                repository.aggiornaUtente(nome,  bio = null, dataNascita = null)
                repository.aggiornaFotoProfilo(fotoProfilo)
                completaPrimoAvvio()

                registrazioneCompletata = true
                Log.d("AppViewModel", "Registrazione completata")
            } catch (e: Exception) {
                Log.e("AppViewModel", "Errore durante la registrazione", e)
            }
        }
    }

    //Funzioni per permessi e mappa
    fun checkLocationPermission(context: Context) : Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    var userLocation by mutableStateOf<Location?>(null)
        private set

    suspend fun getLatLong(hasPermission: Boolean, context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (hasPermission) {
            try {
                val task = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                )

                val location = task.await()
                userLocation = Location(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } catch (e: SecurityException) { //eccezione specifica per evitare warning
                Log.e("AppViewModel", "Warning: ", e)
                userLocation = null
            }
        } else {
            userLocation = null
        }
    }
}