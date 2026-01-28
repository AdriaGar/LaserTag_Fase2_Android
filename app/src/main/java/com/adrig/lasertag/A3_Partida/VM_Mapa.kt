package com.adrig.lasertag.A3_Partida

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adrig.lasertag.data.MapRepository
import com.adrig.lasertag.data.PlayerPosition
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VM_Mapa(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)
    private val repository = MapRepository()

    private val sensorManager by lazy { application.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val rotationSensor: Sensor? by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    private var lastBearing: Float = 0f

    // Map boundaries
    private val latMin = 41.788412
    private val latMax = 41.790269
    private val lonMin = 2.762952
    private val lonMax = 2.771154

    private val _otherPlayers = MutableLiveData<List<PlayerPosition>>()
    val otherPlayers: LiveData<List<PlayerPosition>> = _otherPlayers

    private val _ownPlayerPosition = MutableLiveData<PlayerPosition>()
    val ownPlayerPosition: LiveData<PlayerPosition> = _ownPlayerPosition

    private val LOCATION_INTERVAL = 1000L // Changed to 1 second
    private var pollingJob: Job? = null

    private val jugadorId: String by lazy {
        try {
            Settings.Secure.getString(getApplication<Application>().contentResolver, Settings.Secure.ANDROID_ID)
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

            val currentPosition = PlayerPosition(jugadorId, relX, relY, lastBearing)
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
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        startPolling()
    }

    fun stopLocationUpdates() {
        fusedLocation.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            lastBearing = (azimuth + 360) % 360
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}