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
    private var marker: View? = null
    private lateinit var parent: FrameLayout

    private val LOCATION_INTERVAL = 5000L
    private val PERMISSION_REQUEST = 100

    // Coordenadas del área del mapa (bounds geográficos)
    private val latMin = 41.786000
    private val latMax = 41.786400
    private val lonMin = 2.737100
    private val lonMax = 2.737700

    // Última posición relativa conocida [0,1]
    private var lastXRel: Float = 0.5f
    private var lastYRel: Float = 0.5f

    private var lastLocation: Location? = null

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
        parent = view.findViewById(R.id.mapContainer)

        // Crear marcador
        marker = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(40, 40)
            setBackgroundColor(android.graphics.Color.RED)
            visibility = View.INVISIBLE
        }
        parent.addView(marker)

        // Esperar a que el mapa tenga dimensiones antes de posicionar
        mapImageView.doOnLayout {
            updateMarkerWithLastPosition() // Coloca el marcador usando la última posición relativa
        }

        requestLocationUpdates()
        return view
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
        ).setMinUpdateIntervalMillis(LOCATION_INTERVAL)
            .build()

        try {
            fusedLocation.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val newLocation = result.lastLocation ?: return
            val prevLocation = lastLocation

            // Mantener el marcador si no hay movimiento significativo (>1m)
            if (prevLocation == null || newLocation.distanceTo(prevLocation) > 1f) {
                lastLocation = newLocation
                updateMarker(newLocation) // Recalcula relativas y reposiciona
                Log.d("MapaFragment", "Ubicación actualizada: ${newLocation.latitude}, ${newLocation.longitude}")
            } else {
                updateMarkerWithLastPosition() // Mantiene posición y visibilidad
                Log.d("MapaFragment", "Ubicación sin cambios, marcador mantenido")
            }
        }
    }

    private fun updateMarker(location: Location) {
        // Calcular coordenadas relativas dentro de los bounds del mapa y limitar a [0,1]
        lastXRel = (((location.longitude - lonMin) / (lonMax - lonMin)).toFloat()).coerceIn(0f, 1f)
        // Y invertimos Y para coincidir con la parte superior del mapa como 0
        lastYRel = (1 - ((location.latitude - latMin) / (latMax - latMin)).toFloat()).coerceIn(0f, 1f)

        updateMarkerWithLastPosition()
    }

    private fun updateMarkerWithLastPosition() {
        marker?.let { mk ->
            // Obtener el rectángulo real donde se está dibujando la imagen en el ImageView
            val imageRect = getDisplayedImageRect(mapImageView) ?: return

            // Posicionar dentro del rectángulo de la imagen (no de toda la vista)
            val x = imageRect.left + lastXRel * imageRect.width() - mk.width / 2f
            val y = imageRect.top + lastYRel * imageRect.height() - mk.height / 2f

            Log.d("MapaFragment", "Rect imagen: l=${imageRect.left}, t=${imageRect.top}, w=${imageRect.width()}, h=${imageRect.height()}")
            Log.d("MapaFragment", "Posición marcador: x=$x, y=$y (relX=$lastXRel, relY=$lastYRel)")

            mk.visibility = View.VISIBLE
            mk.x = x
            mk.y = y
        }
    }

    // Calcula el rectángulo de la imagen mostrado dentro del ImageView considerando el scaleType y la imageMatrix
    private fun getDisplayedImageRect(imageView: ImageView): RectF? {
        val d = imageView.drawable ?: return null
        val matrix = imageView.imageMatrix
        val rect = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        matrix.mapRect(rect) // Aplica escala y traslación de la imagen dentro del ImageView

        // La traslación de matrix es relativa al (0,0) del ImageView; rect queda en coordenadas de vista
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
