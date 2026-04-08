package com.adrig.lasertag.A1_Sesion

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrig.lasertag.data.LoginRequest
import com.adrig.lasertag.data.RetrofitClient
import kotlinx.coroutines.launch
import androidx.core.content.edit

class VM_Login : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun loginUser(email: String, password: String, context: Context) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.authService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    if (user != null) {
                        val prefs = context.getSharedPreferences("lasertag", Context.MODE_PRIVATE)
                        prefs.edit { putString("jugador_id", user.id.toString()) }
                    }
                    _loginState.value = LoginState.Success(response.body()?.message ?: "Login correcte")
                } else {
                    _loginState.value = LoginState.Error("Email o contrasenya incorrectes")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error: ${e.message}")
            }
        }
    }
}
