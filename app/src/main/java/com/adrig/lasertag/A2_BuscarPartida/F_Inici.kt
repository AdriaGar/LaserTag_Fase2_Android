package com.adrig.lasertag.A2_BuscarPartida

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.FragmentIniciBinding

class F_Inici: Fragment() {

    lateinit var binding: FragmentIniciBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentIniciBinding.inflate(inflater, container, false)
        return binding.root
    }
}