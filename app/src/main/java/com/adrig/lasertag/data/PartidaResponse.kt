package com.adrig.lasertag.data

data class PartidaResponse(
    val id_partida: String,
    val codi_partida: String,
    val estat: String,
    val mode_joc: String? = null
)
