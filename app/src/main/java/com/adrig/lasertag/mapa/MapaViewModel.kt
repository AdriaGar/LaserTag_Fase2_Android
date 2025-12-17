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
    private val latMin = 41.788412
    private val latMax = 41.790269
    private val lonMin = 2.762952
    private val lonMax = 2.771154

    // FusedLocationProviderClient
    private var fusedLocation: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    //----------------------------------------------------
    // Inicia actualizaciones de ubicación
    //----------------------------------------------------
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        fusedLocation = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    updateUserPosition(location.latitude, location.longitude)
                }
            }
        }

        fusedLocation?.requestLocationUpdates(request, locationCallback!!, context.mainLooper)
    }

    //----------------------------------------------------
    // Detiene actualizaciones de ubicación
    //----------------------------------------------------
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocation?.removeLocationUpdates(it)
        }
    }

    //----------------------------------------------------
    // Actualizar la posición local
    //----------------------------------------------------
    fun updateUserPosition(lat: Double, lon: Double) {
        lastXRel = ((lon - lonMin) / (lonMax - lonMin)).toFloat().coerceIn(0f, 1f)
        lastYRel = (1 - ((lat - latMin) / (latMax - latMin)).toFloat()).coerceIn(0f, 1f)
        userPosition.postValue(Pair(lastXRel, lastYRel))
    }

    //----------------------------------------------------
    // Devuelve la posición actual relativa
    //----------------------------------------------------
    fun getCurrentUserPosition(): Pair<Float, Float> = Pair(lastXRel, lastYRel)

    //----------------------------------------------------
    // Petición unificada: subir local + bajar jugadores
    //----------------------------------------------------
    fun refreshPlayers(localPlayer: Player) {
        viewModelScope.launch {
            try {
                val otherPlayers = repository.updateAndFetchPlayers(localPlayer)
                players.postValue(otherPlayers)
            } catch (e: Exception) {
                Log.e("MapaViewModel", "Error al actualizar jugadores: ${e.message}")
            }
        }
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
