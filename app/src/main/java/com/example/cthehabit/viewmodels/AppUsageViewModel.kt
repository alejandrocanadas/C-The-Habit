package com.example.cthehabit.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.cthehabit.data.entity.AppUsage
import com.example.cthehabit.data.repositories.getUsageStats

class AppUsageViewModel : ViewModel() {

    var apps = mutableStateOf<List<AppUsage>>(emptyList())
        private set

    fun loadApps(context: Context) {
        apps.value = getUsageStats(context)
    }
}