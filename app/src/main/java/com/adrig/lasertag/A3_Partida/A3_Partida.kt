package com.adrig.lasertag.A3_Partida

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.A3PartidaBinding

class A3_Partida : AppCompatActivity() {
    lateinit var binding: A3PartidaBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = A3PartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        // Conecta el BottomNavigationView con el NavController
        binding.bottomNavigationView.setupWithNavController(navController)


    }
}