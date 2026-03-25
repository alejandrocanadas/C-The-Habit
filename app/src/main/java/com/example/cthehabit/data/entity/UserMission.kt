package com.example.cthehabit.data.entity

data class UserMission(
    val id: String = "",
    val activity: String = "",
    val text: String = "",
    val dateAssigned: String = "",
    val completed: Boolean = false,
    val cancelled: Boolean = false
)