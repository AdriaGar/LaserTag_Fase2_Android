package com.adrig.lasertag.data

import android.util.Log

class MapRepository {

    private val apiService = RetrofitClient.instance

    suspend fun sendLocation(position: PlayerPosition) {
        try {
            val response = apiService.sendLocation(position)
            if (response.isSuccessful) {
                Log.d("MapRepository", "Send location success")
            } else {
                Log.e("MapRepository", "Send location failed: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("MapRepository", "Send location error: ${e.message}", e)
        }
    }

    suspend fun getLocations(): List<PlayerPosition> {
        return try {
            val response = apiService.getLocations()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("MapRepository", "Get locations failed: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MapRepository", "Get locations error: ${e.message}", e)
            emptyList()
        }
    }
}
