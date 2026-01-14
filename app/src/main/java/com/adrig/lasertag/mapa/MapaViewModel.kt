package com.adrig.lasertag.mapa

import MapaRepository
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import android.provider.Settings

class MapaViewModel(private val repository: MapaRepository) : ViewModel() {

    // LiveData para la posición del usuario (xRel, yRel) entre 0 y 1
    val userPosition = MutableLiveData<Pair<Float, Float>>()
    val players = MutableLiveData<List<Player>>()

    // ID del jugador local
    var localPlayerId: String = "usuario123"

    // Última posición relativa conocida del jugador local
    private var lastXRel: Float = 0f
    private var lastYRel: Float = 0f

    // Límites del mapa (lat/lon)
    private val latMin = 41.788200
    private val latMax = 41.788600

    private val lonMin = 2.762700
    private val lonMax = 2.763200

    // FusedLocationProviderClient
    private var fusedLocation: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        Log.d("MapaViewModel", "startLocationUpdates() llamado")

        fusedLocation = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location? = result.lastLocation

                if (location == null) {
                    Log.w("MapaViewModel", "onLocationResult: location es null")
                    return
                }

                Log.d(
                    "MapaViewModel",
                    "onLocationResult: lat=${location.latitude}, lon=${location.longitude}"
                )

                updateUserPosition(location.latitude, location.longitude)
            }
        }

        fusedLocation?.requestLocationUpdates(request, locationCallback!!, context.mainLooper)
    }

    fun stopLocationUpdates() {
        Log.d("MapaViewModel", "stopLocationUpdates() llamado")
        locationCallback?.let {
            fusedLocation?.removeLocationUpdates(it)
        }
    }

    fun updateUserPosition(lat: Double, lon: Double) {
        Log.d("MapaViewModel", "updateUserPosition() con lat=$lat lon=$lon")

        val xRel = ((lon - lonMin) / (lonMax - lonMin)).toFloat()
        val yRel = (1 - ((lat - latMin) / (latMax - latMin)).toFloat())

        lastXRel = xRel.coerceIn(0f, 1f)
        lastYRel = yRel.coerceIn(0f, 1f)

        Log.d(
            "MapaViewModel",
            "Convertido a relativas: xRel=$xRel yRel=$yRel -> clamp: lastXRel=$lastXRel lastYRel=$lastYRel"
        )

        userPosition.postValue(Pair(lastXRel, lastYRel))
    }

    fun getCurrentUserPosition(): Pair<Float, Float> {
        Log.d("MapaViewModel", "getCurrentUserPosition() -> xRel=$lastXRel yRel=$lastYRel")
        return Pair(lastXRel, lastYRel)
    }

    fun refreshPlayers(localPlayer: Player) {
        Log.d(
            "MapaViewModel",
            "refreshPlayers() -> enviando al backend: id=${localPlayer.jugador_id}, x=${localPlayer.x}, y=${localPlayer.y}"
        )

        viewModelScope.launch {
            try {
                val otherPlayers = repository.updateAndFetchPlayers(localPlayer)
                Log.d("MapaViewModel", "Jugadores recibidos del backend: $otherPlayers")
                players.postValue(otherPlayers)
            } catch (e: Exception) {
                Log.e("MapaViewModel", "Error al actualizar jugadores: ${e.message}", e)
            }
        }
    }

    fun getDeviceId(context: Context): String {
        val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        Log.d("MapaViewModel", "getDeviceId() -> $id")
        return id
    }
}
