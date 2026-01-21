package com.adrig.lasertag

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adrig.lasertag.data.MapRepository
import com.adrig.lasertag.data.PlayerPosition
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VM_Mapa(application: Application) : AndroidViewModel(application) {

    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)
    private val repository = MapRepository()

    // Map boundaries
    private val latMin = 41.788412
    private val latMax = 41.790269
    private val lonMin = 2.762952
    private val lonMax = 2.771154

    private val _otherPlayers = MutableLiveData<List<PlayerPosition>>()
    val otherPlayers: LiveData<List<PlayerPosition>> = _otherPlayers

    private val _ownPlayerPosition = MutableLiveData<PlayerPosition>()
    val ownPlayerPosition: LiveData<PlayerPosition> = _ownPlayerPosition

    private val LOCATION_INTERVAL = 5000L
    private var pollingJob: Job? = null

    private val jugadorId: String by lazy {
        try {
            android.provider.Settings.Secure.getString(getApplication<Application>().contentResolver, android.provider.Settings.Secure.ANDROID_ID)
                ?: "jugador_local"
        } catch (e: Exception) {
            "jugador_local"
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val newLocation = result.lastLocation ?: return

            val relX = ((newLocation.longitude - lonMin) / (lonMax - lonMin)).coerceIn(0.0, 1.0)
            val relY = (1 - ((newLocation.latitude - latMin) / (latMax - latMin))).coerceIn(0.0, 1.0)

            val currentPosition = PlayerPosition(jugadorId, relX, relY)
            _ownPlayerPosition.postValue(currentPosition)

            viewModelScope.launch {
                repository.sendLocation(currentPosition)
            }
        }
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL)
            .setMinUpdateIntervalMillis(LOCATION_INTERVAL)
            .build()

        fusedLocation.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        startPolling()
    }

    fun stopLocationUpdates() {
        fusedLocation.removeLocationUpdates(locationCallback)
        stopPolling()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                fetchLocationsFromServer()
                delay(LOCATION_INTERVAL)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
    }

    private suspend fun fetchLocationsFromServer() {
        val allPlayers = repository.getLocations()
        val otherPlayers = allPlayers.filter { it.id != jugadorId }
        _otherPlayers.postValue(otherPlayers)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
