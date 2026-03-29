package com.example.cthehabit.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.cthehabit.data.repositories.getUsageLast24h
import com.example.cthehabit.data.repositories.getUsageLast7Days

class AppUsageViewModel : ViewModel() {

    // día -> app -> tiempo (ms)
    var usageData = mutableStateOf<Map<String, Map<String, Long>>>(emptyMap())
        private set

    fun loadToday(context: Context) {
        usageData.value = getUsageLast24h(context)
    }

    fun loadLast7Days(context: Context) {
        usageData.value = getUsageLast7Days(context)
    }
}