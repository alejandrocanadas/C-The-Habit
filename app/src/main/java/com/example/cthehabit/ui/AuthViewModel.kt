package com.example.cthehabit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cthehabit.data.local.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val sessionManager: SessionManager,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    // Estado de login
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Cargando
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Comprobar token al iniciar ViewModel
        viewModelScope.launch {
            val token = sessionManager.getTokenOnce()
            _isLoggedIn.value = token != null
        }
    }

    // 🔹 Login
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // 🔹 Opción 1: sin variable token innecesaria
                withContext(Dispatchers.IO) {
                    auth.signInWithEmailAndPassword(email, password).await()
                    auth.currentUser?.uid?.let { sessionManager.saveAuthToken(it) }
                }

                _isLoggedIn.value = true
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Error al iniciar sesión"
                _errorMessage.value = msg
                onError(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔹 Registro
    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // 🔹 Opción 1: sin variable token innecesaria
                withContext(Dispatchers.IO) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    auth.currentUser?.uid?.let { sessionManager.saveAuthToken(it) }
                }

                _isLoggedIn.value = true
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Error al registrarse"
                _errorMessage.value = msg
                onError(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔹 Logout
    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                auth.signOut()
                sessionManager.clearSession()
            }
            _isLoggedIn.value = false
            onComplete()
        }
    }
}

// 🔹 Factory para Compose
class AuthViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}