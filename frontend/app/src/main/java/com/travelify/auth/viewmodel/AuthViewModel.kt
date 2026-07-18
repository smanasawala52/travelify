package com.travelify.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.travelify.auth.api.ApiClient
import com.travelify.auth.api.AuthService
import com.travelify.auth.api.LoginRequest
import com.travelify.auth.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val authService: AuthService = ApiClient.create(tokenManager)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        checkForToken()
    }

    private fun checkForToken() {
        viewModelScope.launch {
            _isLoggedIn.value = tokenManager.getToken() != null
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            try {
                val response = authService.login(LoginRequest(email, pass))
                tokenManager.saveToken(response.token)
                _isLoggedIn.value = true
            } catch (e: Exception) {
                // Handle login error
                _isLoggedIn.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.deleteToken()
            _isLoggedIn.value = false
        }
    }
}