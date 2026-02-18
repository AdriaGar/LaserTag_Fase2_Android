package com.adrig.lasertag.A2_BuscarPartida

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.A2BuscarPartidaBinding

class A2_BuscarPartida : AppCompatActivity() {

    lateinit var binding: A2BuscarPartidaBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = A2BuscarPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        // Conecta el BottomNavigationView con el NavController
        binding.bottomNavigationView.setupWithNavController(navController)

    }
}