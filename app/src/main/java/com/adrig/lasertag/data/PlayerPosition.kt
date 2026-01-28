package com.adrig.lasertag.data

import com.google.gson.annotations.SerializedName

data class PlayerPosition(
    @SerializedName("jugador_id") val id: String,
    @SerializedName("relX") val relX: Double,
    @SerializedName("relY") val relY: Double,
    @SerializedName("bearing") val bearing: Float = 0f
)
