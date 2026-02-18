package com.adrig.lasertag.A3_Partida

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adrig.lasertag.databinding.FragmentMarcadorBinding

class F_Marcador : Fragment() {

    lateinit var binding: FragmentMarcadorBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMarcadorBinding.inflate(layoutInflater)
        return binding.root

    }


}