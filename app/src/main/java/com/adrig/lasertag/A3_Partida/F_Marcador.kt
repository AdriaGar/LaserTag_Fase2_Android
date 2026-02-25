package com.adrig.lasertag.A3_Partida

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.adrig.lasertag.data.PlayerScore
import com.adrig.lasertag.databinding.FragmentMarcadorBinding

class F_Marcador : Fragment() {

    private var _binding: FragmentMarcadorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarcadorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Reemplazar esto con los datos reales del ViewModel
        val sampleScores = listOf(
            PlayerScore("Jugador 1", 15, 3),
            PlayerScore("Jugador 2", 12, 5),
            PlayerScore("Jugador 3", 10, 8),
            PlayerScore("Jugador 4", 8, 10),
            PlayerScore("Jugador 5", 5, 12),
            PlayerScore("Jugador 6", 2, 15)
        )

        val adapter = MarcadorAdapter(sampleScores)
        binding.recyclerViewMarcador.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewMarcador.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}