package com.adrig.lasertag.A1_Sesion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrig.lasertag.data.RegisterRequest
import com.adrig.lasertag.data.RetrofitClient
import kotlinx.coroutines.launch

class VM_Registro : ViewModel() {

    private val _registrationState = MutableLiveData<RegistrationState>(RegistrationState.Idle)
    val registrationState: LiveData<RegistrationState> = _registrationState

    fun registerUser(nomUsuari: String, nom: String, cognoms: String, email: String, contrasenya: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            try {
                val response = RetrofitClient.instance.register(
                    RegisterRequest(nomUsuari, nom, cognoms, email, contrasenya)
                )
                if (response.isSuccessful && response.body() != null) {
                    _registrationState.value = RegistrationState.Success(response.body()?.message ?: "Registre completat")
                } else {
                    _registrationState.value = RegistrationState.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _registrationState.value = RegistrationState.Error("Error de connexió: ${e.message}")
            }
        }
    }
}
