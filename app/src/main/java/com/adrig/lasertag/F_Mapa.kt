package com.adrig.lasertag

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class F_Mapa : Fragment() {

    private val viewModel: VM_Mapa by viewModels()

    private lateinit var mapImageView: ImageView
    private lateinit var parent: FrameLayout
    private var ownMarker: View? = null

    private val PERMISSION_REQUEST = 100

    private val otherMarkers = mutableMapOf<String, View>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mapa, container, false)
        mapImageView = view.findViewById(R.id.mapImageView)
        parent = view.findViewById(R.id.mapContainer)

        ownMarker = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(40, 40)
            setBackgroundColor(android.graphics.Color.RED)
            visibility = View.INVISIBLE
        }
        parent.addView(ownMarker)

        mapImageView.viewTreeObserver.addOnGlobalLayoutListener {
            if (mapImageView.drawable != null) {
                updateMarkers()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            viewModel.startLocationUpdates()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
    }

    private fun observeViewModel() {
        viewModel.ownPlayerPosition.observe(viewLifecycleOwner) { position ->
            updateMarkerPosition(ownMarker, position)
        }
        viewModel.otherPlayers.observe(viewLifecycleOwner) { players ->
            updateOtherPlayersMarkers(players)
        }
    }

    private fun updateMarkers() {
        viewModel.ownPlayerPosition.value?.let { updateMarkerPosition(ownMarker, it) }
        viewModel.otherPlayers.value?.let { updateOtherPlayersMarkers(it) }
    }

    private fun updateMarkerPosition(marker: View?, position: PlayerPosition?) {
        if (marker == null || position == null) return

        val imageRect = getDisplayedImageRect(mapImageView) ?: return

        val x = imageRect.left + (position.relX * imageRect.width()).toFloat() - marker.width / 2f
        val y = imageRect.top + (position.relY * imageRect.height()).toFloat() - marker.height / 2f

        marker.visibility = View.VISIBLE
        marker.x = x
        marker.y = y
    }

    private fun updateOtherPlayersMarkers(players: List<PlayerPosition>) {
        val imageRect = getDisplayedImageRect(mapImageView) ?: return

        val currentPlayerIds = players.map { it.id }.toSet()
        val markersToRemove = otherMarkers.keys.filter { it !in currentPlayerIds }
        markersToRemove.forEach { id ->
            otherMarkers[id]?.let { parent.removeView(it) }
            otherMarkers.remove(id)
        }

        for (player in players) {
            val markerView = otherMarkers[player.id] ?: run {
                val v = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(35, 35)
                    setBackgroundColor(android.graphics.Color.BLUE)
                }
                parent.addView(v)
                otherMarkers[player.id] = v
                v
            }
            updateMarkerPosition(markerView, player)
        }
    }

    private fun getDisplayedImageRect(imageView: ImageView): RectF? {
        val d = imageView.drawable ?: return null
        val matrix = imageView.imageMatrix
        val rect = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        matrix.mapRect(rect)
        return rect
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.startLocationUpdates()
        } else {
            Log.e("F_Mapa", "Permiso de ubicación denegado")
        }
    }
}
