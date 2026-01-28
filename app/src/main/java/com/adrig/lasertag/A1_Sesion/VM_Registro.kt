package com.adrig.lasertag.A1_Sesion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val message: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class VM_Registro : ViewModel() {

    private val _registrationState = MutableLiveData<RegistrationState>(RegistrationState.Idle)
    val registrationState: LiveData<RegistrationState> = _registrationState

    fun registerUser(nomUsuari: String, usuari: String, cognoms: String, email: String, contrasenya: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            // Simula una llamada a la red
            delay(1500)

            // TODO: Implementar la lógica de registro real con el servidor
            if (nomUsuari.isNotEmpty() && usuari.isNotEmpty() && cognoms.isNotEmpty() && email.isNotEmpty() && contrasenya.isNotEmpty()) {
                _registrationState.value = RegistrationState.Success("Registre completat amb èxit!")
            } else {
                _registrationState.value = RegistrationState.Error("Hi ha hagut un error durant el registre.")
            }
        }
    }
}
