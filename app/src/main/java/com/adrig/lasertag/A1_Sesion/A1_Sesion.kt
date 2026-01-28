package com.adrig.lasertag.A1_Sesion

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.A1SesionBinding

class A1_Sesion : AppCompatActivity() {
    lateinit var binding: A1SesionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = A1SesionBinding.inflate(layoutInflater)
        setContentView(R.layout.a1_sesion)

    }
}