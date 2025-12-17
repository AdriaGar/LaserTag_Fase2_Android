package com.adrig.lasertag.mapa

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MapaApi {

    @POST("ubicaciones")
    suspend fun updateAndGetPlayers(
        @Body localPlayer: Player
    ): List<Player>
}

