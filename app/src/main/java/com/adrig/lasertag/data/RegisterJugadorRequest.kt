package com.adrig.lasertag.data

data class RegisterJugadorRequest(
    val id_jugador: String,
    val nom: String,
    val email: String = ""
)