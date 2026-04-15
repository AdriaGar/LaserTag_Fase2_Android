package com.adrig.lasertag.A3_Partida

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.adrig.lasertag.databinding.FragmentMarcadorBinding

class F_Marcador : Fragment() {

    private var _binding: FragmentMarcadorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VM_Marcador by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarcadorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("lasertag", Context.MODE_PRIVATE)

        val idPartida = prefs.getString("id_partida", "") ?: ""
        val jugadorId = prefs.getString("jugador_id", "") ?: ""

        binding.recyclerViewMarcador.layoutManager = LinearLayoutManager(context)

        viewModel.ranking.observe(viewLifecycleOwner) { llista ->
            binding.recyclerViewMarcador.adapter = MarcadorAdapter(llista)
        }

        viewModel.estatPropi.observe(viewLifecycleOwner) { estat ->
            binding.tvKills.text = estat.kills.toString()
            binding.tvMorts.text = estat.morts.toString()
            binding.tvPunts.text = estat.punts.toString()
            binding.tvViu.text = if (estat.viu) "VIU" else "ELIMINAT"
            binding.tvViu.setTextColor(if (estat.viu) android.graphics.Color.GREEN else android.graphics.Color.RED)
        }

        if (idPartida.isNotEmpty() && jugadorId.isNotEmpty()) {
            viewModel.startPolling(idPartida, jugadorId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}