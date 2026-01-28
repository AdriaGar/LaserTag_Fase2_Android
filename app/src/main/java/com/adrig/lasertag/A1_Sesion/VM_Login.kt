package com.adrig.lasertag.A1_Sesion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val message: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class VM_Sesion : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            // Simula una llamada a la red
            delay(1500)

            // TODO: Implementar la lógica de login real con el servidor
            if (username.isNotEmpty() && password.isNotEmpty()) {
                _loginState.value = LoginState.Success("Inici de sessió correcte")
            } else {
                _loginState.value = LoginState.Error("Usuari o contrasenya incorrectes")
            }
        }
    }
}
