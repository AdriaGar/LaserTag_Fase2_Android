package com.adrig.lasertag.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LocationService {

    @POST("ubicacion")
    suspend fun sendLocation(@Body position: PlayerPosition): Response<Unit>

    @GET("ubicaciones")
    suspend fun getLocations(): Response<List<PlayerPosition>>
}
