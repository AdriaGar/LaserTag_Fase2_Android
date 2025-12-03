package com.adrig.lasertag

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*

class MapaFragment : Fragment() {

    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var mapImageView: ImageView
    private lateinit var parent: FrameLayout

    private var marker: View? = null

    private val LOCATION_INTERVAL = 5000L
    private val PERMISSION_REQUEST = 100

    // Server base URL (replace <IP_DEL_SERVIDOR> with real IP or hostname)
    // We use a runtime getter so we can fallback to emulator loopback when running on emulator.
    private fun getServerBase(): String {
        // IMPORTANT: prefer the LAN server IP by default. Use the emulator loopback
        // 10.0.2.2 only when the server is running on the same host machine as the
        // Android emulator. If your server is on another device (e.g. 192.168.0.100),
        // the emulator's 10.0.2.2 will NOT reach it and will produce a timeout.
        val USE_EMULATOR_LOOPBACK = false // set true only if server runs on emulator host
        return if (USE_EMULATOR_LOOPBACK && isEmulator()) {
            "http://10.0.2.2:3000"
        } else {
            "http://192.168.0.100:3000"
        }
    }
    private val POST_PATH = "/ubicacion"
    private val GET_PATH = "/ubicaciones"

    // Polling handler for fetching other players
    private val pollingHandler = Handler(Looper.getMainLooper())
    private val pollingIntervalMs = 5000L
    private val pollingRunnable = object : Runnable {
        override fun run() {
            fetchLocationsFromServer()
            pollingHandler.postDelayed(this, pollingIntervalMs)
        }
    }

    // Local jugador id placeholder until real ID system exists
    private val jugadorId: String by lazy {
        try {
            Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
                ?: "jugador_local"
        } catch (e: Exception) {
            "jugador_local"
        }
    }

    // Bounds del mapa
    private val latMin = 41.788412
    private val latMax = 41.790269
    private val lonMin = 2.771154
    private val lonMax = 2.762952

    private var lastXRel: Float = 0.5f
    private var lastYRel: Float = 0.5f
    private var lastLocation: Location? = null

    data class OtherPlayer(val id: String, val lat: Double, val lon: Double)

    private val otherPlayers = mutableListOf<OtherPlayer>()
    private val otherMarkers = mutableMapOf<String, View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mapa, container, false)
        mapImageView = view.findViewById(R.id.mapImageView)
        mapImageView.doOnLayout {
            updateMarkerWithLastPosition()
        }
        parent = view.findViewById(R.id.mapContainer)

        // Marcador del usuario (rojo)
        marker = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(40, 40)
            setBackgroundColor(android.graphics.Color.RED)
            visibility = View.INVISIBLE
        }
        parent.addView(marker)


        addTestPlayers()


        mapImageView.viewTreeObserver.addOnGlobalLayoutListener {
            val drawable = mapImageView.drawable
            if (drawable != null) {
                Log.d("MapaFragment", "Imagen del mapa cargada, ahora sí se pueden pintar marcadores")
                updateMarkerWithLastPosition()
                updateOtherPlayersMarkers()
            } else {
                Log.e("MapaFragment", "drawable aún NULL – el mapa no está listo")
            }
        }

        requestLocationUpdates()
        // Start polling other players' locations
        pollingHandler.post(pollingRunnable)
        return view
    }

    private fun addTestPlayers() {
        otherPlayers.clear()
        otherPlayers.add(OtherPlayer("p1", 41.788500, 2.763000))
        otherPlayers.add(OtherPlayer("p2", 41.788900, 2.763500))
        otherPlayers.add(OtherPlayer("p3", 41.789200, 2.764000))
        otherPlayers.add(OtherPlayer("p4", 41.789500, 2.765000))
        otherPlayers.add(OtherPlayer("p5", 41.788800, 2.766500))
        otherPlayers.add(OtherPlayer("p6", 41.789100, 2.767500))
    }

    //Marcadores otros jugadores
    private fun updateOtherPlayersMarkers() {

        val imageRect = getDisplayedImageRect(mapImageView)
        if (imageRect == null) {
            Log.e("MapaFragment", "imageRect NULL – no se puede pintar jugadores")
            return
        }

        Log.d("MapaFragment", "Pintando ${otherPlayers.size} jugadores")

        for (player in otherPlayers) {

            val xRel = (((player.lon - lonMin) / (lonMax - lonMin)).toFloat()).coerceIn(0f, 1f)
            val yRel = (1 - ((player.lat - latMin) / (latMax - latMin)).toFloat()).coerceIn(0f, 1f)

            val markerView = otherMarkers[player.id] ?: run {
                val v = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(35, 35)
                    setBackgroundColor(android.graphics.Color.BLUE)
                    visibility = View.VISIBLE
                }
                parent.addView(v)
                otherMarkers[player.id] = v
                v
            }

            val x = imageRect.left + xRel * imageRect.width() - markerView.width / 2f
            val y = imageRect.top + yRel * imageRect.height() - markerView.height / 2f

            markerView.x = x
            markerView.y = y
        }
    }


    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationUpdates() {
        if (!hasLocationPermission()) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST)
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL
        ).setMinUpdateIntervalMillis(LOCATION_INTERVAL).build()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("MapaFragment", "No hay permiso de ubicación")
            return
        }

        fusedLocation.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val newLocation = result.lastLocation ?: return

            // Keep lastLocation for fallback, but DO NOT update the red marker locally.
            // We treat the server as the source of truth for marker placement.
            lastLocation = newLocation

            // Send this device location to server (background). After successful POST,
            // we'll refresh via GET so the red marker is placed from server data.
            sendLocationToServer(newLocation)
        }
    }

    private fun updateMarker(location: Location) {
        lastXRel = (((location.longitude - lonMin) / (lonMax - lonMin)).toFloat()).coerceIn(0f, 1f)
        lastYRel = (1 - ((location.latitude - latMin) / (latMax - latMin)).toFloat()).coerceIn(0f, 1f)

        mapImageView.doOnLayout {
            updateMarkerWithLastPosition()
        }
    }


    private fun updateMarkerWithLastPosition() {
        val mk = marker ?: return
        val imageRect = getDisplayedImageRect(mapImageView) ?: return

        if (imageRect.width() <= 0 || imageRect.height() <= 0) {
            Log.e("MapaFragment", "imageRect amb dimensions invàlides")
            return
        }

        val x = imageRect.left + lastXRel * imageRect.width() - mk.width / 2f
        val y = imageRect.top + lastYRel * imageRect.height() - mk.height / 2f

        mk.visibility = View.VISIBLE
        mk.x = x
        mk.y = y
    }



    private fun getDisplayedImageRect(imageView: ImageView): RectF? {
        val d = imageView.drawable ?: return null
        val matrix = imageView.imageMatrix

        val rect = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        matrix.mapRect(rect)
        return rect
    }

    // Detect simple emulator environments so we can use 10.0.2.2 for host machine
    private fun isEmulator(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT ?: ""
        val model = android.os.Build.MODEL ?: ""
        val product = android.os.Build.PRODUCT ?: ""
        val manufacturer = android.os.Build.MANUFACTURER ?: ""
        return fingerprint.contains("generic") || fingerprint.contains("unknown") ||
                model.contains("Emulator") || model.contains("Android SDK built for x86") ||
                product.contains("sdk") || manufacturer.contains("Genymotion")
    }

    // Send current location to server (POST /ubicacion)
    private fun sendLocationToServer(location: Location) {
        Thread {
            try {
                val url = URL(getServerBase() + POST_PATH)
                Log.d("MapaFragment", "POST -> ${url}")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.doOutput = true

                val json = JSONObject()
                json.put("jugador_id", jugadorId)
                json.put("lat", location.latitude)
                json.put("lng", location.longitude)

                val out: OutputStream = conn.outputStream
                out.write(json.toString().toByteArray(Charsets.UTF_8))
                out.flush()
                out.close()

                val code = conn.responseCode
                if (code in 200..299) {
                    Log.d("MapaFragment", "POST ubicacion OK: $code")
                    // Refresh from server so the red marker is placed from server data
                    fetchLocationsFromServer()
                } else {
                    Log.e("MapaFragment", "POST ubicacion failed: $code")
                    // Try to log response body if present
                    try {
                        val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                        if (!err.isNullOrEmpty()) Log.e("MapaFragment", "POST error body: $err")
                    } catch (_: Exception) {}
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("MapaFragment", "Error sending location", e)
            }
        }.start()
    }

    // Fetch all locations from server (GET /ubicaciones)
    private fun fetchLocationsFromServer() {
        Thread {
            try {
                val url = URL(getServerBase() + GET_PATH)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/json")

                val code = conn.responseCode
                if (code in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val sb = StringBuilder()
                    var line = reader.readLine()
                    while (line != null) {
                        sb.append(line)
                        line = reader.readLine()
                    }
                    reader.close()

                    val arr = JSONArray(sb.toString())
                    val newOthers = mutableListOf<OtherPlayer>()
                    var ownPlayerFromServer: OtherPlayer? = null

                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val id = o.optString("jugador_id", "p_$i")
                        val lat = o.optDouble("lat", 0.0)
                        val lng = o.optDouble("lng", 0.0)
                        if (id == jugadorId) {
                            // Save our own location as reported by server
                            ownPlayerFromServer = OtherPlayer(id, lat, lng)
                        } else {
                            newOthers.add(OtherPlayer(id, lat, lng))
                        }
                    }

                    activity?.runOnUiThread {
                        // Update other players (blue markers)
                        otherPlayers.clear()
                        otherPlayers.addAll(newOthers)
                        updateOtherPlayersMarkers()

                        // If the server returned our own location, use it to place the red marker.
                        // Otherwise, fall back to the lastLocation from GPS (if available).
                        if (ownPlayerFromServer != null) {
                            val me = ownPlayerFromServer
                            Log.d("MapaFragment", "Ubicación propia obtenida del servidor: ${me.lat}, ${me.lon}")
                            lastXRel = (((me.lon - lonMin) / (lonMax - lonMin)).toFloat()).coerceIn(0f, 1f)
                            lastYRel = (1 - ((me.lat - latMin) / (latMax - latMin)).toFloat()).coerceIn(0f, 1f)
                            updateMarkerWithLastPosition()
                        } else if (lastLocation != null) {
                            // Fallback: use last known GPS location to position the red marker
                            Log.d("MapaFragment", "Servidor no devuelve nuestro jugador; usando GPS local como fallback")
                            val loc = lastLocation!!
                            lastXRel = (((loc.longitude - lonMin) / (lonMax - lonMin)).toFloat()).coerceIn(0f, 1f)
                            lastYRel = (1 - ((loc.latitude - latMin) / (latMax - latMin)).toFloat()).coerceIn(0f, 1f)
                            updateMarkerWithLastPosition()
                        }
                    }
                } else {
                    Log.e("MapaFragment", "GET ubicaciones failed: $code")
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("MapaFragment", "Error fetching locations", e)
            }
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted — start location updates
                requestLocationUpdates()
            } else {
                Log.e("MapaFragment", "Permiso de ubicación denegado por el usuario")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocation.removeLocationUpdates(locationCallback)
        pollingHandler.removeCallbacks(pollingRunnable)
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapaFragment().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}
