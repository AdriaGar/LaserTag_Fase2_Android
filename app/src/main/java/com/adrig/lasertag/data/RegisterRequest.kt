package com.adrig.lasertag.data

data class RegisterRequest(
    val nom_usuari: String,
    val nom: String,
    val cognoms: String,
    val email: String,
    val contrasenya: String
)