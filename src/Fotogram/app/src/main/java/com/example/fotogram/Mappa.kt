package com.example.fotogram

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fotogram.ui.components.Button
import com.example.fotogram.viewmodel.AppViewModel
import com.example.fotogram.viewmodel.FotogramViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation

//scegliere punto di partenza del simulatore cliccando su 3 pallini sopra il simulatore -> Location
@Composable
fun Mappa(modifier: Modifier, appViewModel: AppViewModel, viewModel: FotogramViewModel,
          onBack: () -> Unit) {
    var hasPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            Log.d("Mappa", "Permessi ottenuti")
        } else {
            Log.d("Mappa", "Permessi non ottenuti")
        }
    }

    LaunchedEffect(Unit) {
        hasPermission = appViewModel.checkLocationPermission(context)
        if (hasPermission) {
            Log.d("Mappa", "Posso calcolare la posizione")
        } else {
            Log.d("Mappa", "Richiedo i permessi")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    //Eseguito ogni volta che cambia hasPermission
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            Log.d("Mappa", "Recupero coordinate")
            appViewModel.getLatLong(hasPermission, context)
        }
    }

    val arrivoDaBacheca = viewModel.selectedPostLocation != null

    //Mappa in alto, pannello info in basso
    Column(modifier = modifier.fillMaxSize()) {
        if (arrivoDaBacheca) {
            val locationUser = appViewModel.userLocation
            val locationPost = viewModel.selectedPostLocation
            Log.d("Mappa", "Coordinate utente: $locationUser")
            Log.d("Mappa", "Coordinate post: $locationPost")

            val mapViewportState = rememberMapViewportState {
                if (locationPost == null) { //centra su posizione dell'utente
                    setCameraOptions {
                        center(Point.fromLngLat(
                            locationUser?.longitude ?: 0.0,
                            locationUser?.latitude ?: 0.0
                        ))
                        zoom(15.5)
                    }
                } else { //centra su posizione del post
                    setCameraOptions {
                        center(Point.fromLngLat(
                            locationPost.longitude ?: 0.0,
                            locationPost.latitude ?: 0.0
                        ))
                        zoom(15.5)
                    }
                }
                /* I gesti touch simulati dal mouse sono:
                    Click singolo → tap
                    Doppio click LMB → zoom in
                    Doppio click RMB → zoom out
                    Ctrl + trascina su/giù → pinch to zoom */
            }

            //Mappa
            MapboxMap(modifier = Modifier.fillMaxWidth().weight(1f),
                mapViewportState = mapViewportState
            ) {
                val markerIconUser = rememberIconImage(
                    key = R.drawable.position_marker_user,
                    painter = painterResource(R.drawable.position_marker_user)
                )
                val markerIconPost = rememberIconImage(
                    key = R.drawable.position_marker_post,
                    painter = painterResource(R.drawable.position_marker_post)
                )

                //Mostra marker utente solo se vicino al post
                if (isVicino(locationUser, locationPost)) {
                    PointAnnotation(
                        point = Point.fromLngLat(
                            locationUser?.longitude ?: 0.0,
                            locationUser?.latitude ?: 0.0
                        )
                    ) {
                        iconImage = markerIconUser
                    }
                }

                if (locationPost != null) {
                    PointAnnotation(
                        point = Point.fromLngLat(
                            locationPost.longitude ?: 0.0,
                            locationPost.latitude ?: 0.0
                        )
                    ) {
                        iconImage = markerIconPost
                    }
                }
            }

            //Pannello info sotto la mappa
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Legenda", style = MaterialTheme.typography.titleSmall)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.position_marker_post),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Posizione del post", style = MaterialTheme.typography.bodyMedium)
                }

                if (isVicino(locationUser, locationPost)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.position_marker_user),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("La tua posizione", style = MaterialTheme.typography.bodyMedium)
                    }

                    //Distanza tra utente e post
                    if (distanzaKm != null) {
                        Text(
                            text = if (distanzaKm < 1) "Sei a ${"%.0f".format(distanzaKm * 1000)} metri dal post"
                            else "Sei a ${"%.1f".format(distanzaKm)} km dal post",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    Text(
                        text = "Sei troppo lontano per vedere la tua posizione sulla mappa",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {    //arrivo da CreaPost, mostro spinner finché non ho la posizione
            if (appViewModel.userLocation == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return
            }

            val locationUser = appViewModel.userLocation
            val selectedPoint by viewModel.selectedPoint.collectAsState()

            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(
                        locationUser?.longitude ?: 0.0,
                        locationUser?.latitude ?: 0.0
                    ))
                    zoom(15.5)
                }
            }

            //Mappa
            MapboxMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                mapViewportState = mapViewportState,
                onMapClickListener = { point ->
                    viewModel.setSelectedPoint(point) //il punto scelto è salvato nello state
                    Log.d("mapclick", "cliccato")
                    true //ritorna true per gestire l'evento
                }
            ) {
                val markerIconUser = rememberIconImage(
                    key = R.drawable.position_marker_user,
                    painter = painterResource(R.drawable.position_marker_user)
                )

                PointAnnotation(
                    point = Point.fromLngLat(
                        locationUser?.longitude ?: 0.0,
                        locationUser?.latitude ?: 0.0
                    )
                ) {
                    iconImage = markerIconUser
                }

                val markerSelectedPoint = rememberIconImage(
                    key = R.drawable.selected_point_marker,
                    painter = painterResource(R.drawable.selected_point_marker)
                )

                selectedPoint?.let { point ->
                    Log.d("punto", "punto: ${point.coordinates()}")
                    PointAnnotation(point = point) {
                        iconImage = markerSelectedPoint
                    }
                }
            }

            //Pannello info sotto la mappa
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (selectedPoint == null) "Tocca sulla mappa per selezionare una posizione"
                    else "Posizione selezionata",
                    style = MaterialTheme.typography.bodyMedium
                )

                //Coordinate in tempo reale
                selectedPoint?.let { point ->
                    Text(
                        text = "Lat: ${"%.5f".format(point.latitude())} Lon: ${"%.5f".format(point.longitude())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Button(
                        text = "Rimuovi posizione",
                        onClick = { viewModel.rimuoviPunto() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

//Funzione per calcolare distanza
var distanzaKm: Float = 0.0F
fun isVicino(locationUser: Location?, locationPost: Location?, sogliaKm: Double = 10.0 ): Boolean {
    //evitano errori compilatore nullPointerException; il compilatore fa lo smart cast automaticamente
    if (locationUser == null || locationPost == null) return false
    if (locationUser.latitude == null || locationUser.longitude == null) return false
    if (locationPost.latitude == null || locationPost.longitude == null) return false

    val results = FloatArray(1)
    //distanceBetween: funzione di Android che calcola la distanza in metri tra 2 coordinate
    android.location.Location.distanceBetween(
        locationUser.latitude,
        locationUser.longitude,
        locationPost.latitude,
        locationPost.longitude,
        results
    )
    distanzaKm = results[0] / 1000
    Log.d("Mappa", "Distanza: $distanzaKm")
    return distanzaKm <= sogliaKm
}