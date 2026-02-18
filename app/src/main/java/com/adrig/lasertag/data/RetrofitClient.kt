package com.adrig.lasertag.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val AUTH_BASE_URL = "http://192.168.0.194:5050/"
    private const val LOCATION_BASE_URL = "http://192.168.0.100:3000/"

    private val authRetrofit = Retrofit.Builder()
        .baseUrl(AUTH_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val locationRetrofit = Retrofit.Builder()
        .baseUrl(LOCATION_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authService: AuthService by lazy {
        authRetrofit.create(AuthService::class.java)
    }

    val locationService: LocationService by lazy {
        locationRetrofit.create(LocationService::class.java)
    }
}
