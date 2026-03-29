package com.example.cthehabit.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cthehabit.data.local.SessionManager
import com.example.cthehabit.data.repositories.FirestoreRepository
import com.example.cthehabit.data.repositories.getUsageLast24h
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class AuthViewModel(
    private val sessionManager: SessionManager,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestoreRepo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Login
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

    // Registro
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

    // Logout
    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    auth.signOut()
                    sessionManager.clearSession()
                }
                _isLoggedIn.value = false
                _errorMessage.value = null
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cerrar sesión"
            }
        }
    }

    // Guardar cuestionario
    fun saveQuestionnaire(
        answers: Map<Int, List<String>>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val firestoreAnswers = answers.mapKeys { "q${it.key}" }
            firestoreRepo.saveQuestionnaire(firestoreAnswers)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Error al guardar") }
        }
    }

    // Guardar uso diario
    fun saveUsageEvent(
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val usageList = withContext(Dispatchers.IO) {

                    getUsageLast24h(context)
                }

                // Convertir Map<String, Map<String, Long>> a Map<String, Long> sumando uso por app
                val usageMap = usageList.values.fold(mutableMapOf<String, Long>()) { acc, dayMap ->
                    dayMap.forEach { (pkg, duration) ->
                        acc[pkg] = (acc[pkg] ?: 0L) + duration
                    }
                    acc
                }

                val today = LocalDate.now().toString()
                firestoreRepo.saveUsageEvent(today, usageMap.mapValues { it.value })
                    .onSuccess { onSuccess() }
                    .onFailure { onError(it.message ?: "Error al guardar uso") }

            } catch (e: Exception) {
                onError(e.message ?: "Error al obtener uso de apps")
            }
        }
    }
}

// Factory
class AuthViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}