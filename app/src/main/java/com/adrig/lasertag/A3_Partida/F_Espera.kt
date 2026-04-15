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
        binding.rvEquip1.layoutManager = LinearLayoutManager(context)
        binding.rvEquip2.layoutManager = LinearLayoutManager(context)

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
                                val idPartida = partida?.id_partida ?: ""
                                val mode = partida?.mode_joc

                                if (mode == "per_equips" || mode == "captura_bandera") {
                                    pintarEquips(idPartida)
                                } else {
                                    pintarJugadorsBarrejats(idPartida)
                                }
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
                delay(2000)
            }
        }
    }

    private suspend fun pintarJugadorsBarrejats(idPartida: String) {
        binding.layoutEquips.visibility = View.GONE
        binding.rvJugadorsEspera.visibility = View.VISIBLE

        val resJugadors = RetrofitClient.gameService.getJugadorsPartida(idPartida)
        if (resJugadors.isSuccessful) {
            val jugadors = resJugadors.body().orEmpty()
            binding.rvJugadorsEspera.adapter = EsperaJugadorsAdapter(jugadors)
        }
    }

    private suspend fun pintarEquips(idPartida: String) {
        binding.rvJugadorsEspera.visibility = View.GONE
        binding.layoutEquips.visibility = View.VISIBLE

        val resEquips = RetrofitClient.gameService.getEquipsPartida(idPartida)
        if (!resEquips.isSuccessful) return

        val equips = resEquips.body().orEmpty().take(2)
        val equip1 = equips.getOrNull(0)
        val equip2 = equips.getOrNull(1)

        binding.tvEquip1.text = equip1?.nom ?: "Equip 1"
        binding.tvEquip2.text = equip2?.nom ?: "Equip 2"

        val jugadors1 = equip1?.let {
            RetrofitClient.gameService.getJugadorsEquip(it.id_equip).body().orEmpty()
        }.orEmpty()
        val jugadors2 = equip2?.let {
            RetrofitClient.gameService.getJugadorsEquip(it.id_equip).body().orEmpty()
        }.orEmpty()

        binding.rvEquip1.adapter = EsperaJugadorsAdapter(jugadors1)
        binding.rvEquip2.adapter = EsperaJugadorsAdapter(jugadors2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
