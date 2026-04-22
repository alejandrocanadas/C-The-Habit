package com.example.cthehabit.utils

val questions = listOf(
    Question(
        text = "¿Cuántas horas al día pasas en redes sociales?",
        options = listOf(
            "1 a 2 horas",
            "2 a 4 horas",
            "3 a 5 horas",
            "+5 horas"
        ),
        multipleSelection = false,
        minSelections = 1
    ),
    Question(
        text = "¿En qué momento del día las utilizas más?",
        options = listOf(
            "Al despertar",
            "Estudiar / trabajar",
            "Tiempo libre",
            "Antes de dormir"
        ),
        multipleSelection = false,
        minSelections = 1
    ),
    Question(
        text = "¿Qué te gustaría hacer más en tu tiempo libre?",
        options = listOf(
            "Deporte",
            "Leer",
            "Relajarte",
            "Tocar un instrumento",
            "Cocinar",
            "Aprender un idioma",
            "Escribir",
            "Programar",
            "Dibujar"
        ),
        multipleSelection = true,
        minSelections = 3
    )
)