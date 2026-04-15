package com.adrig.lasertag.A2_BuscarPartida

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adrig.lasertag.A3_Partida.A3_Partida
import com.adrig.lasertag.data.RegisterJugadorRequest
import com.adrig.lasertag.data.RetrofitClient
import com.adrig.lasertag.databinding.FragmentCodiBinding
import kotlinx.coroutines.launch
import androidx.core.content.edit

class F_Codi: Fragment() {

    private var _binding: FragmentCodiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodiBinding.inflate(inflater, container, false)

        binding.btnEntrar.setOnClickListener {
            val codi = binding.etCodi.text.toString().trim()
            if (codi.isNotEmpty()) {
                unirJugadorAPartida(codi)
            } else {
                Toast.makeText(requireContext(), "Introdueix un codi", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun unirJugadorAPartida(codi: String) {
        val prefs = requireContext().getSharedPreferences("lasertag", Context.MODE_PRIVATE)
        val jugadorId = prefs.getString("jugador_id", null)
        val jugadorNom = prefs.getString("jugador_nom", null)
        val jugadorEmail = prefs.getString("jugador_email", "") ?: ""

        if (jugadorId == null) {
            Toast.makeText(requireContext(), "Has d'iniciar sessió primer", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val nomPerRegistrar = (jugadorNom?.takeIf { it.isNotBlank() } ?: jugadorId)
                RetrofitClient.gameService.registrarJugador(
                    RegisterJugadorRequest(
                        id_jugador = jugadorId,
                        nom = nomPerRegistrar,
                        email = jugadorEmail,
                    )
                )

                val response = RetrofitClient.gameService.unirPartida(
                    mapOf("id_jugador" to jugadorId, "codi" to codi)
                )

                if (response.isSuccessful) {
                    val estatRes = RetrofitClient.gameService.getEstatPartida(codi)
                    if (estatRes.isSuccessful && estatRes.body()?.estat == "acabada") {
                        Toast.makeText(requireContext(), "Aquesta partida ja ha finalitzat", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    val idPartidaReal = response.body()?.id_partida ?: ""
                    prefs.edit {
                        putString("codi_sala", codi)
                        putString("id_partida", idPartidaReal)
                    }

                    val intent = Intent(requireActivity(), A3_Partida::class.java).apply {
                        putExtra("CODI_SALA", codi)
                        putExtra("ESTAT_INICIAL", estatRes.body()?.estat ?: "pendent")
                    }
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    val missatge = when (response.code()) {
                        404 -> "Codi de sala incorrecte"
                        409 -> "No et pots unir a la partida (conflicte)"
                        else -> "No s'ha pogut unir (${response.code()})"
                    }
                    Toast.makeText(requireContext(), missatge, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de connexió", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}