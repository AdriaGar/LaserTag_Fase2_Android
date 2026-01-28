package com.adrig.lasertag.A2_BuscarPartida

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adrig.lasertag.A3_Partida.A3_Partida
import com.adrig.lasertag.MainActivity
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.FragmentCodiBinding

class F_Codi: Fragment() {

    lateinit var binding : FragmentCodiBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCodiBinding.inflate(layoutInflater)

        binding.btnEntrar.setOnClickListener {
            val intent = Intent(requireActivity(), A3_Partida::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return binding.root
    }
}