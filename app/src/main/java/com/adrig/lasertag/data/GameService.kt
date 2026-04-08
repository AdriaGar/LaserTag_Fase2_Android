package com.adrig.lasertag.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GameService {
    @POST("jugador/registrar")
    suspend fun registrarJugador(@Body request: RegisterJugadorRequest): Response<Unit>

    @POST("partida/unir")
    suspend fun unirPartida(@Body body: Map<String, String>): Response<UnirPartidaResponse>

    @GET("partida/{id}/jugadors")
    suspend fun getJugadorsPartida(@Path("id") idPartida: String): Response<List<JugadorPartidaResponse>>

    @GET("game/estat/{id_jugador}")
    suspend fun getJugadorEstat(@Path("id_jugador") idJugador: String): Response<PlayerStatusResponse>

    @GET("partida/estat")
    suspend fun getEstatPartida(@Query("codi") codi: String): Response<PartidaResponse>

    @GET("partida/{id}/equips")
    suspend fun getEquipsPartida(@Path("id") idPartida: String): Response<List<EquipResponse>>

    @GET("partida/estadistiques")
    suspend fun getEstadistiquesPartida(@Query("id") idPartida: String): Response<ScoreboardResponse>
}