package com.adrig.lasertag.A3_Partida

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adrig.lasertag.A2_BuscarPartida.A2_BuscarPartida
import com.adrig.lasertag.R
import com.adrig.lasertag.data.RetrofitClient
import com.adrig.lasertag.data.JugadorScore
import com.adrig.lasertag.databinding.FragmentFEsperaBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

class F_Espera : Fragment() {

    private var _binding: FragmentFEsperaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFEsperaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = requireContext().getSharedPreferences("lasertag", Context.MODE_PRIVATE)
        val codi = activity?.intent?.getStringExtra("CODI_SALA") ?: prefs.getString("codi_sala", "") ?: ""
        
        binding.tvCodiSala.text = "Codi: $codi"
        binding.rvJugadorsEspera.layoutManager = LinearLayoutManager(context)

        if (codi.isNotEmpty()) {
            prefs.edit { putString("codi_sala", codi) }
            iniciarBucleEstat(codi, prefs)
        }
    }

    private fun iniciarBucleEstat(codi: String, prefs: android.content.SharedPreferences) {
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                try {
                    val resEstat = RetrofitClient.gameService.getEstatPartida(codi)
                    if (resEstat.isSuccessful) {
                        val partida = resEstat.body()

                        when (partida?.estat) {
                            "jugant" -> {
                                findNavController().navigate(R.id.action_f_Espera_to_f_Mapa)
                                break
                            }
                            "acabada" -> {
                                Toast.makeText(requireContext(), "La partida ha finalitzat", Toast.LENGTH_SHORT).show()
                                prefs.edit { remove("codi_sala") }
                                val intent = Intent(requireContext(), A2_BuscarPartida::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().finish()
                                break
                            }
                            else -> {
                                val resJugadors = RetrofitClient.gameService.getJugadorsPartida(partida?.id_partida ?: "")
                                if (resJugadors.isSuccessful) {
                                    val jugadors = resJugadors.body()?.map { 
                                        JugadorScore(it.id_jugador, it.nom, it.nickname, 0, 0, 0, 0)
                                    } ?: emptyList()
                                    binding.rvJugadorsEspera.adapter = MarcadorAdapter(jugadors)
                                }
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
                delay(2000)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
