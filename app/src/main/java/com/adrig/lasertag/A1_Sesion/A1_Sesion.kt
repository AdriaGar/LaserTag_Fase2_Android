package com.adrig.lasertag.A1_Sesion

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.A1SesionBinding

class A1_Sesion : AppCompatActivity() {
    private lateinit var binding: A1SesionBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = A1SesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        // Conecta el BottomNavigationView con el NavController
        binding.bottomNavigationView.setupWithNavController(navController)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_a1_sesion, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.f_Login -> {
                navController.navigate(R.id.f_Login)
                true
            }
            R.id.f_Registre -> {
                navController.navigate(R.id.f_Registre)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
