package com.adrig.lasertag.A3_Partida

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.A3PartidaBinding

class A3_Partida : AppCompatActivity() {
    lateinit var binding: A3PartidaBinding
    private lateinit var navController: NavController

    // For screen timeout
    private val screenTimeoutHandler = Handler(Looper.getMainLooper())
    private var screenTimeoutRunnable: Runnable? = null
    private val SCREEN_TIMEOUT_MS = 15000L

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = A3PartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        binding.navigationRail.setupWithNavController(navController)

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
        setupScreenTimeout()
    }

    override fun onResume() {
        super.onResume()
        resetScreenTimeout()
    }

    override fun onPause() {
        super.onPause()
        stopScreenTimeout()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetScreenTimeout()
    }

    private fun setupScreenTimeout() {
        // Este runnable atenuará el brillo de la pantalla
        screenTimeoutRunnable = Runnable {
            val attributes = window.attributes
            attributes.screenBrightness = 0.05f // Brillo muy bajo
            window.attributes = attributes
        }
    }

    private fun resetScreenTimeout() {
        // Cancela el apagado programado
        screenTimeoutRunnable?.let { screenTimeoutHandler.removeCallbacks(it) }

        // Restaura el brillo al valor por defecto del sistema
        val attributes = window.attributes
        attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE // Valor por defecto
        window.attributes = attributes

        // Mantiene la pantalla encendida
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Programa el próximo apagado
        screenTimeoutRunnable?.let { screenTimeoutHandler.postDelayed(it, SCREEN_TIMEOUT_MS) }
    }

    private fun stopScreenTimeout() {
        // Cancela el apagado programado
        screenTimeoutRunnable?.let { screenTimeoutHandler.removeCallbacks(it) }

        // Permite que la pantalla se apague de forma normal
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}