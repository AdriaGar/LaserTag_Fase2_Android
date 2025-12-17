package com.adrig.lasertag.mapa

import MapaRepository
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.adrig.lasertag.databinding.FragmentMapaBinding

class MapaFragment : Fragment() {

    private lateinit var binding: FragmentMapaBinding
    private lateinit var mapImageView: ImageView
    private lateinit var parent: FrameLayout

    private var userMarker: View? = null
    private val playerMarkers = mutableMapOf<String, View>()

    private val PERMISSION_REQUEST = 100
    private val pollingHandler = Handler(Looper.getMainLooper())
    private lateinit var pollingRunnable: Runnable

    private val repository = MapaRepository(RetrofitClient.api)
    private val mapaViewModel = MapaViewModel(repository)

    private lateinit var idUser : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        idUser = mapaViewModel.getDeviceId(context!!)
        binding = FragmentMapaBinding.inflate(inflater, container, false)
        mapImageView = binding.mapImageView
        parent = binding.mapContainer

        setupUserMarker()
        observeViewModel()

        if (!hasLocationPermission()) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST)
        } else {
            mapaViewModel.startLocationUpdates(requireContext())
        }

        setupPolling()

        return binding.root
    }

    private fun setupUserMarker() {
        userMarker = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(40, 40)
            setBackgroundColor(Color.RED)
            visibility = View.INVISIBLE
        }
        parent.addView(userMarker)
    }

    private fun observeViewModel() {
        mapaViewModel.userPosition.observe(viewLifecycleOwner) { (xRel, yRel) ->
            drawUserMarker(xRel, yRel)
        }

        mapaViewModel.players.observe(viewLifecycleOwner) { players ->
            drawPlayers(players)
        }
    }

    private fun drawUserMarker(xRel: Float, yRel: Float) {
        val marker = userMarker ?: return
        val imageRect = getDisplayedImageRect(mapImageView) ?: return

        val x = imageRect.left + xRel * imageRect.width() - marker.width / 2f
        val y = imageRect.top + yRel * imageRect.height() - marker.height / 2f

        marker.visibility = View.VISIBLE
        marker.x = x
        marker.y = y
    }

    private fun drawPlayers(players: List<Player>) {
        val imageRect = getDisplayedImageRect(mapImageView) ?: return

        players.forEach { player ->
            val marker = playerMarkers[player.jugador_id] ?: createPlayerMarker(player.jugador_id)

            val x = imageRect.left + player.lon * imageRect.width() - marker.width / 2f
            val y = imageRect.top + player.lat * imageRect.height() - marker.height / 2f

            marker.x = x.toFloat()
            marker.y = y.toFloat()
        }
    }

    private fun createPlayerMarker(id: String): View {
        val marker = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(35, 35)
            setBackgroundColor(Color.BLUE)
        }
        parent.addView(marker)
        playerMarkers[id] = marker
        return marker
    }

    private fun getDisplayedImageRect(imageView: ImageView): RectF? {
        val drawable = imageView.drawable ?: return null
        val rect = RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        imageView.imageMatrix.mapRect(rect)
        return rect
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pollingHandler.removeCallbacks(pollingRunnable)
        mapaViewModel.stopLocationUpdates()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            mapaViewModel.startLocationUpdates(requireContext())
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Polling unificado cada 5 segundos
    private fun setupPolling() {
        pollingRunnable = object : Runnable {
            override fun run() {
                val (xRel, yRel) = mapaViewModel.getCurrentUserPosition()
                val localPlayer = Player(
                    jugador_id = mapaViewModel.localPlayerId,
                    lat = yRel.toDouble(),
                    lon = xRel.toDouble(),
                )

                mapaViewModel.refreshPlayers(localPlayer)

                pollingHandler.postDelayed(this, 1000L)
            }
        }
        pollingHandler.post(pollingRunnable)
    }
}
