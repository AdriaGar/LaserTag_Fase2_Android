package com.adrig.lasertag.A1_Sesion

open class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val message: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}