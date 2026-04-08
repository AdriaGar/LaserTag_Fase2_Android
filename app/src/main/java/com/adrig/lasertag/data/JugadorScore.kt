package com.adrig.lasertag.data

data class JugadorScore(
    val id_jugador: String,
    val nom: String,
    val nickname: String?,
    val punts: Int,
    val kills: Int,
    val morts: Int,
    val posicio: Int
)