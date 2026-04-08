package com.adrig.lasertag.A1_Sesion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.adrig.lasertag.A2_BuscarPartida.A2_BuscarPartida
import com.adrig.lasertag.A3_Partida.A3_Partida
import com.adrig.lasertag.R
import com.adrig.lasertag.data.RetrofitClient
import com.adrig.lasertag.databinding.A1SesionBinding
import kotlinx.coroutines.launch

class A1_Sesion : AppCompatActivity() {
    private lateinit var binding: A1SesionBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("lasertag", Context.MODE_PRIVATE)
        val jugadorId = prefs.getString("jugador_id", null)
        val codiSala = prefs.getString("codi_sala", null)

        if (jugadorId != null) {
            if (codiSala != null) {
                lifecycleScope.launch {
                    try {
                        val res = RetrofitClient.gameService.getEstatPartida(codiSala)
                        if (res.isSuccessful) {
                            val estat = res.body()?.estat
                            if (estat == "pendent" || estat == "jugant") {

                                val intent = Intent(this@A1_Sesion, A3_Partida::class.java).apply {
                                    putExtra("CODI_SALA", codiSala)
                                    putExtra("ESTAT_INICIAL", estat)
                                }
                                startActivity(intent)
                                finish()
                                return@launch
                            }
                        }
                        prefs.edit { remove("codi_sala") }
                        startActivity(Intent(this@A1_Sesion, A2_BuscarPartida::class.java))
                        finish()
                    } catch (e: Exception) {
                        startActivity(Intent(this@A1_Sesion, A2_BuscarPartida::class.java))
                        finish()
                    }
                }
            } else {
                startActivity(Intent(this, A2_BuscarPartida::class.java))
                finish()
            }
        } else {
            iniciarInterficie()
        }
    }

    private fun iniciarInterficie() {
        binding = A1SesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
    }
}
