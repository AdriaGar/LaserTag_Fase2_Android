package com.adrig.lasertag

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.location.Location
import android.os.Bundle
import android.os.Looper
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

    // Bounds del mapa
    private val latMin = 41.786000
    private val latMax = 41.786400
    private val lonMin = 2.737100
    private val lonMax = 2.737700

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
                Log.e("MapaFragment", "⚠ drawable aún NULL – el mapa no está listo")
            }
        }

        requestLocationUpdates()
        return view
    }

    private fun addTestPlayers() {
        otherPlayers.clear()
        otherPlayers.add(OtherPlayer("p1", 41.786150, 2.737300))
        otherPlayers.add(OtherPlayer("p2", 41.786350, 2.737500))
        otherPlayers.add(OtherPlayer("p3", 41.786250, 2.737200))
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

            if (lastLocation == null || newLocation.distanceTo(lastLocation!!) > 1f) {
                lastLocation = newLocation
                updateMarker(newLocation)
            } else {
                updateMarkerWithLastPosition()
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocation.removeLocationUpdates(locationCallback)
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
