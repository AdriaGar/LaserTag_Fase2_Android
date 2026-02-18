package com.adrig.lasertag.A1_Sesion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrig.lasertag.data.LoginRequest
import com.adrig.lasertag.data.RetrofitClient
import kotlinx.coroutines.launch

class VM_Login : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.authService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    _loginState.value = LoginState.Success(loginResponse?.message ?: "Inici de sessió correcte")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _loginState.value = LoginState.Error("Email o contrasenya incorrectes: $errorBody")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error de connexió: ${e.message}")
            }
        }
    }
}
