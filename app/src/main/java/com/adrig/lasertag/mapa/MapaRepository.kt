import com.adrig.lasertag.mapa.MapaApi
import com.adrig.lasertag.mapa.Player

class MapaRepository(private val api: MapaApi) {

    suspend fun updateAndFetchPlayers(localPlayer: Player): List<Player> {
        return api.updateAndGetPlayers(localPlayer)
    }
}


