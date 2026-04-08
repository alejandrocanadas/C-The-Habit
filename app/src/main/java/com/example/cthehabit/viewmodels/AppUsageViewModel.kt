package com.example.cthehabit.viewmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cthehabit.data.repositories.getUsageLast24h
import com.example.cthehabit.data.repositories.getUsageLast7Days
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppUsageViewModel : ViewModel() {

    var usageData = mutableStateOf<Map<String, Map<String, Long>>>(emptyMap())
        private set

    private var currentMode: String = "24h"
    private var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    private var appContext: Context? = null

    fun loadToday(context: Context) {
        currentMode = "24h"
        val ctx = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            val data = getUsageLast24h(ctx)
            withContext(Dispatchers.Main) {   // ← actualiza en Main para que Compose lo vea
                usageData.value = data
            }
        }
    }

    fun loadLast7Days(context: Context) {
        currentMode = "7d"
        val ctx = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            val data = getUsageLast7Days(ctx)
            withContext(Dispatchers.Main) {
                usageData.value = data
            }
        }
    }

    fun observeSyncAndAutoRefresh(context: Context) {
        if (prefListener != null) return

        appContext = context.applicationContext

        // Carga inmediata al abrir la app — equivale a presionar "Calcular Métricas 24h"
        viewModelScope.launch(Dispatchers.IO) {
            val data = getUsageLast24h(appContext!!)
            withContext(Dispatchers.Main) {
                usageData.value = data
            }
        }

        val prefs = appContext!!.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "last_sync_time") {
                val ctx = appContext ?: return@OnSharedPreferenceChangeListener
                viewModelScope.launch(Dispatchers.IO) {
                    val data = if (currentMode == "7d") getUsageLast7Days(ctx)
                    else getUsageLast24h(ctx)
                    withContext(Dispatchers.Main) {
                        usageData.value = data
                    }
                }
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    override fun onCleared() {
        super.onCleared()
        appContext?.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            ?.unregisterOnSharedPreferenceChangeListener(prefListener)
        prefListener = null
        appContext = null
    }
}