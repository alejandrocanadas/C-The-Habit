package com.example.cthehabit.utils

data class Question(
    val text: String,
    val options: List<String>,
    val multipleSelection: Boolean = false,
    val minSelections: Int = 1
)
