package com.adrig.lasertag.data

import com.google.gson.annotations.SerializedName

data class JugadorsStats(
    @SerializedName("id_jugador") val id: String,
    @SerializedName("nom") val nom: String,
    @SerializedName("punts") val punts: Int,
    @SerializedName("kills") val kills: Int,
    @SerializedName("morts") val morts: Int
)