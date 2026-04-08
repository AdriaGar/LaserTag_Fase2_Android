package com.adrig.lasertag.data

data class PlayerStatusResponse(
    val id_jugador: String,
    val id_partida: String?,
    val viu: Boolean,
    val pot_disparar: Boolean,
    val kills: Int,
    val morts: Int,
    val punts: Int
)
