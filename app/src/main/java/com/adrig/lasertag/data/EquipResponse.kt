package com.adrig.lasertag.data

data class EquipResponse(
    val id_equip: String,
    val nom: String,
    val color: String?,
    val jugadors: List<String>? = null,
    val id_partida: String
)