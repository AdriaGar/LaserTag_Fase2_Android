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

    fun registerUser(nom: String, cognoms: String, email: String, contrasenya: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading


            val request = RegisterRequest(nom = nom, cognoms = cognoms, email = email, password = contrasenya)

            try {
                val response = RetrofitClient.authService.register(request)

                if (response.isSuccessful) {
                    _registrationState.value = RegistrationState.Success(response.body()?.message ?: "Usuari creat correctament!")
                } else {
                    val errorMsg = response.errorBody()?.string()
                    _registrationState.value = RegistrationState.Error("Error en el registre: $errorMsg")
                }
            } catch (e: Exception) {
                _registrationState.value = RegistrationState.Error("Error de connexió: ${e.message}")
            }
        }
    }
}
