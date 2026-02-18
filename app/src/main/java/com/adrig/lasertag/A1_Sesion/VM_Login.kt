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

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.instance.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    _loginState.value = LoginState.Success("Inici de sessió correcte: ${response.body()?.usuari}")
                } else {
                    _loginState.value = LoginState.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error de connexió: ${e.message}")
            }
        }
    }
}
