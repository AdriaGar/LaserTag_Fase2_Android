package com.adrig.lasertag.A3_Partida

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.adrig.lasertag.A2_BuscarPartida.A2_BuscarPartida
import com.adrig.lasertag.R
import com.adrig.lasertag.data.RetrofitClient
import com.adrig.lasertag.databinding.A3PartidaBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

class A3_Partida : AppCompatActivity() {
    lateinit var binding: A3PartidaBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = A3PartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        val codiSala = intent.getStringExtra("CODI_SALA") ?: ""
        val estatInicial = intent.getStringExtra("ESTAT_INICIAL") ?: "pendent"
        
        val graph = navController.navInflater.inflate(R.navigation.nav_a3_partida)
        graph.setStartDestination(if (estatInicial == "jugant") R.id.f_Mapa else R.id.f_Espera)
        navController.graph = graph

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.navigationRail.visibility = if (destination.id == R.id.f_Espera) View.GONE else View.VISIBLE
        }

        binding.navigationRail.setupWithNavController(navController)
        
        setupMicButton()
        setupScreenTimeout()

        if (codiSala.isNotEmpty()) iniciarVigilanciaPartida(codiSala)
    }

    private fun iniciarVigilanciaPartida(codi: String) {
        lifecycleScope.launch {
            while (isActive) {
                try {
                    val res = RetrofitClient.gameService.getEstatPartida(codi)
                    if (res.isSuccessful && res.body()?.estat == "acabada") {
                        Toast.makeText(this@A3_Partida, "La partida ha finalitzat", Toast.LENGTH_LONG).show()
                        getSharedPreferences("lasertag", Context.MODE_PRIVATE).edit { remove("codi_sala") }
                        val intent = Intent(this@A3_Partida, A2_BuscarPartida::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        break
                    }
                } catch (e: Exception) { e.printStackTrace() }
                delay(3000) // Comprovació cada 3 segons per eficiència
            }
        }
    }

    private fun setupMicButton() {
        val micButton = binding.navigationRail.findViewById<View>(R.id.action_mic)
        val originalIconTint = binding.navigationRail.itemIconTintList
        micButton?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.navigationRail.itemIconTintList = ColorStateList.valueOf(Color.RED)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.navigationRail.itemIconTintList = originalIconTint
                    true
                }
                else -> false
            }
        }
    }

    private val screenTimeoutHandler = Handler(Looper.getMainLooper())
    private var screenTimeoutRunnable: Runnable? = null
    private val SCREEN_TIMEOUT_MS = 15000L

    override fun onResume() { super.onResume(); resetScreenTimeout() }
    override fun onPause() { super.onPause(); stopScreenTimeout() }
    override fun onUserInteraction() { super.onUserInteraction(); resetScreenTimeout() }

    private fun setupScreenTimeout() {
        screenTimeoutRunnable = Runnable {
            val attributes = window.attributes
            attributes.screenBrightness = 0.05f
            window.attributes = attributes
        }
    }

    private fun resetScreenTimeout() {
        screenTimeoutRunnable?.let { screenTimeoutHandler.removeCallbacks(it) }
        val attributes = window.attributes
        attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = attributes
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        screenTimeoutRunnable?.let { screenTimeoutHandler.postDelayed(it, SCREEN_TIMEOUT_MS) }
    }

    private fun stopScreenTimeout() {
        screenTimeoutRunnable?.let { screenTimeoutHandler.removeCallbacks(it) }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
