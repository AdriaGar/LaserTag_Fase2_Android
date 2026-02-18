package com.adrig.lasertag.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("registre")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("ubicacion")
    suspend fun sendLocation(@Body position: PlayerPosition): Response<Unit>

    @GET("ubicaciones")
    suspend fun getLocations(): Response<List<PlayerPosition>>
}
