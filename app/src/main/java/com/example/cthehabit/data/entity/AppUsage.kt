package com.example.cthehabit.data.entity

data class AppUsage(
    val packageName: String,
    val timeInForeground: Long,
    val lastTimeUsed: Long
)