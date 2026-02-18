package com.adrig.lasertag.data

class MapRepository {
    private val service = RetrofitClient.locationService

    suspend fun sendLocation(position: PlayerPosition) {
        try {
            service.sendLocation(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getLocations(): List<PlayerPosition> {
        return try {
            val response = service.getLocations()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
