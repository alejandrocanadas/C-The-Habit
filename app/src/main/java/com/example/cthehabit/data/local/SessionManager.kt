package com.example.cthehabit.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "session_prefs")

class SessionManager(context: Context) {
    private val appContext = context.applicationContext

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    suspend fun saveAuthToken(token: String) {
        appContext.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    val authToken: Flow<String?> = appContext.dataStore.data.map { it[TOKEN_KEY] }

    suspend fun getTokenOnce(): String? {
        return appContext.dataStore.data.map { it[TOKEN_KEY] }.firstOrNull()
    }


    suspend fun clearSession() {
        appContext.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}