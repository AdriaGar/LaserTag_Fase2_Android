package com.adrig.lasertag.data

import com.google.gson.annotations.SerializedName

data class GameStateResponse(
    @SerializedName("jugadores") val jugadors: List<JugadorsStats>
)