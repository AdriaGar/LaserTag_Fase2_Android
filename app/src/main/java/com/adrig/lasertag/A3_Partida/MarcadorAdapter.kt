package com.adrig.lasertag.A3_Partida

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adrig.lasertag.R
import com.adrig.lasertag.data.PlayerScore

class MarcadorAdapter(private val scores: List<PlayerScore>) : RecyclerView.Adapter<MarcadorAdapter.ScoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_marcador_jugador, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position]
        holder.bind(score)
    }

    override fun getItemCount(): Int = scores.size

    class ScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.textViewPlayerName)
        private val kills: TextView = itemView.findViewById(R.id.textViewKills)
        private val deaths: TextView = itemView.findViewById(R.id.textViewDeaths)

        fun bind(score: PlayerScore) {
            playerName.text = score.playerName
            kills.text = score.kills.toString()
            deaths.text = score.deaths.toString()
        }
    }
}