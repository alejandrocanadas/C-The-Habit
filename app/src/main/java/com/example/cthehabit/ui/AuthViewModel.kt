package com.example.cthehabit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cthehabit.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    var isLoading: Boolean = false
        private set

    var errorMessage: String? = null
        private set

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val result = repo.login(email, password)
            isLoading = false
            result
                .onSuccess { onSuccess() }
                .onFailure { e -> errorMessage = e.message ?: "Error al iniciar sesión" }
        }
    }

    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val result = repo.register(email, password)
            isLoading = false
            result
                .onSuccess { onSuccess() }
                .onFailure { e -> errorMessage = e.message ?: "Error al registrarse" }
        }
    }
}