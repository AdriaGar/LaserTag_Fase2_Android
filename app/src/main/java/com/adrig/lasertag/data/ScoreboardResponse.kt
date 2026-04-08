package com.adrig.lasertag.data

data class ScoreboardResponse(
    val id_partida: String,
    val ranking: List<JugadorScore>,
    val total: Int
)