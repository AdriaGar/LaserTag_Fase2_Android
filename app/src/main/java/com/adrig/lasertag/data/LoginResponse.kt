package com.adrig.lasertag.data

data class LoginResponse(
    val missatge: String,
    val usuari: String,
    val rol: String,
    val nom: String,
    val cognoms: String,
    val email: String
)