package com.adrig.lasertag.A3_Partida

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adrig.lasertag.data.JugadorPartidaResponse
import com.adrig.lasertag.databinding.ItemEsperaJugadorBinding

class EsperaJugadorsAdapter(
    private val jugadors: List<JugadorPartidaResponse>
) : RecyclerView.Adapter<EsperaJugadorsAdapter.EsperaViewHolder>() {

    class EsperaViewHolder(val binding: ItemEsperaJugadorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EsperaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEsperaJugadorBinding.inflate(inflater, parent, false)
        return EsperaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EsperaViewHolder, posicio: Int) {
        val jugador = jugadors[posicio]
        holder.binding.tvJugadorNom.text = jugador.nom

        if (jugador.nickname.isNullOrBlank()) {
            holder.binding.tvJugadorNickname.visibility = View.GONE
        } else {
            holder.binding.tvJugadorNickname.visibility = View.VISIBLE
            holder.binding.tvJugadorNickname.text = jugador.nickname
        }

    }


    override fun getItemCount(): Int = jugadors.size


}

