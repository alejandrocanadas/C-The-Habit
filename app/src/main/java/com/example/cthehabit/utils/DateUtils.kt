package com.example.cthehabit.utils


import java.text.SimpleDateFormat
import java.util.*

fun formatDate(time: Long): String {

    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(time))
}

fun getTodayDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .format(Date())
}