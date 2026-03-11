package com.example.cthehabit.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.cthehabit.data.entity.AppUsage
import com.example.cthehabit.data.repositories.getUsageStats
import com.example.cthehabit.data.repositories.getUsageStatsLast7Days

class AppUsageViewModel : ViewModel() {

    var apps = mutableStateOf<List<AppUsage>>(emptyList())
        private set

    fun loadToday(context: Context) {
        apps.value = getUsageStats(context)
    }

    fun loadLast7Days(context: Context) {
        apps.value = getUsageStatsLast7Days(context)
    }
}