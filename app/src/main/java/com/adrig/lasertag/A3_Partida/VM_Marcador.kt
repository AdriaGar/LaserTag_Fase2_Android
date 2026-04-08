package com.adrig.lasertag.A3_Partida

import androidx.lifecycle.*
import com.adrig.lasertag.data.RetrofitClient
import com.adrig.lasertag.data.JugadorScore
import com.adrig.lasertag.data.PlayerStatusResponse
import kotlinx.coroutines.*

class VM_Marcador : ViewModel() {
    private val _ranking = MutableLiveData<List<JugadorScore>>()
    val ranking: LiveData<List<JugadorScore>> = _ranking

    private val _estatPropi = MutableLiveData<PlayerStatusResponse>()
    val estatPropi: LiveData<PlayerStatusResponse> = _estatPropi

    fun startPolling(idPartida: String, jugadorId: String) {
        viewModelScope.launch {
            while (isActive) {
                try {

                    val resRanking = RetrofitClient.gameService.getEstadistiquesPartida(idPartida)
                    if (resRanking.isSuccessful) {
                        _ranking.postValue(resRanking.body()?.ranking)
                    }

                    val resEstat = RetrofitClient.gameService.getJugadorEstat(jugadorId)
                    if (resEstat.isSuccessful) {
                        resEstat.body()?.let { _estatPropi.postValue(it) }
                    }
                } catch (e: Exception) { e.printStackTrace() }
                delay(2000)
            }
        }
    }
}