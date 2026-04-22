package com.example.cthehabit.utils

object MissionGenerator {

    fun getMinutesFromHours(hoursAnswer: String): Int {
        return when (hoursAnswer.trim()) {
            "1 a 2 horas" -> 15
            "2 a 4 horas" -> 20
            "3 a 5 horas" -> 25
            "+5 horas", "Más de 5 horas" -> 30
            else -> 15
        }
    }

    fun generateMissions(
        hoursAnswer: String,
        momentAnswer: String,
        selectedActivities: List<String>
    ): List<Mission> {
        val minutes = getMinutesFromHours(hoursAnswer)

        return selectedActivities.mapNotNull { activity ->
            val missionText = getMissionText(activity, momentAnswer, minutes)
            missionText?.let {
                val prefix = getCategoryPrefix(activity)
                Mission(
                    activity = activity,
                    text = "$prefix $it"
                )
            }
        }
    }

    private fun getCategoryPrefix(activity: String): String {
        return when (activity) {
            "Deporte" -> "Deporte:"
            "Leer" -> "Leer:"
            "Relajarte" -> "Relajarte:"
            "Tocar un instrumento" -> "Instrumento:"
            "Cocinar" -> "Cocinar:"
            "Aprender un idioma" -> "Idioma:"
            "Escribir" -> "Escribir:"
            "Programar" -> "Programar:"
            "Dibujar" -> "Dibujar:"
            else -> ""
        }
    }

    private fun randomMission(options: List<String>): String {
        return options.random()
    }

    private fun getMissionText(
        activity: String,
        moment: String,
        minutes: Int
    ): String? {
        return when (activity) {

            "Deporte" -> when (moment) {
                "Al despertar" -> randomMission(listOf(
                    "Activa tu cuerpo con estiramientos durante $minutes minutos sin parar",
                    "Muévete antes de usar el celular durante $minutes minutos",
                    "Haz movilidad articular completa",
                    "Realiza una rutina corta sin pausas",
                    "Activa tu energía con saltos y estiramientos",
                    "Respira y mueve tu cuerpo conscientemente"
                ))
                "Estudiar / trabajar" -> randomMission(listOf(
                    "Levántate y camina durante $minutes minutos",
                    "Haz una pausa activa sin distracciones",
                    "Estira espalda y cuello",
                    "Muévete para despejar la mente",
                    "Haz ejercicios ligeros sin celular",
                    "Activa tu cuerpo con movimiento continuo"
                ))
                "Tiempo libre" -> randomMission(listOf(
                    "Haz actividad física durante $minutes minutos",
                    "Baila sin parar con tu música favorita",
                    "Prueba un ejercicio nuevo",
                    "Sal a caminar y observa tu entorno",
                    "Muévete libremente sin estructura",
                    "Haz una rutina ligera al aire libre"
                ))
                "Antes de dormir" -> randomMission(listOf(
                    "Estira tu cuerpo durante $minutes minutos",
                    "Respira profundo y relaja tensión",
                    "Haz estiramientos suaves",
                    "Libera tensión con movimientos lentos",
                    "Relaja músculos conscientemente",
                    "Desconecta tu cuerpo antes de dormir"
                ))
                else -> null
            }

            "Leer" -> when (moment) {
                "Al despertar" -> randomMission(listOf(
                    "Lee durante $minutes minutos sin interrupciones",
                    "Empieza el día leyendo algo que te guste",
                    "Lee 5-10 páginas con enfoque",
                    "Lee algo inspirador",
                    "Dedica tiempo a leer sin distracciones",
                    "Lee en silencio total"
                ))
                "Estudiar / trabajar" -> randomMission(listOf(
                    "Lee durante $minutes minutos sin distraerte",
                    "Cambia de actividad leyendo",
                    "Encuentra una idea interesante",
                    "Lee con total concentración",
                    "Desconéctate leyendo",
                    "Lee sin usar el celular"
                ))
                "Tiempo libre" -> randomMission(listOf(
                    "Lee durante $minutes minutos concentrado",
                    "Avanza varias páginas sin pausas",
                    "Sumérgete en la historia",
                    "Lee en un ambiente tranquilo",
                    "Disfruta la lectura sin distracciones",
                    "Lee algo nuevo"
                ))
                "Antes de dormir" -> randomMission(listOf(
                    "Lee antes de dormir sin celular",
                    "Lee algo relajante",
                    "Cierra el día leyendo",
                    "Lee con luz tenue",
                    "Desconecta con lectura",
                    "Relájate leyendo"
                ))
                else -> null
            }

            "Relajarte" -> when (moment) {
                "Al despertar" -> randomMission(listOf(
                    "Respira profundo durante $minutes minutos",
                    "Conecta contigo en silencio",
                    "Toma algo caliente sin celular",
                    "Empieza el día en calma",
                    "Haz una pausa consciente",
                    "Siéntate sin distracciones"
                ))
                "Estudiar / trabajar" -> randomMission(listOf(
                    "Cierra los ojos y respira",
                    "Haz una mini meditación",
                    "Aléjate de pantallas",
                    "Relaja tu mente unos minutos",
                    "Respira profundo conscientemente",
                    "Descansa sin estímulos"
                ))
                "Tiempo libre" -> randomMission(listOf(
                    "Escucha música y relájate",
                    "No hagas nada por $minutes minutos",
                    "Medita sin distracciones",
                    "Desconéctate completamente",
                    "Relaja cuerpo y mente",
                    "Descansa en silencio"
                ))
                "Antes de dormir" -> randomMission(listOf(
                    "Apaga estímulos fuertes",
                    "Haz respiraciones profundas",
                    "Relájate sin pantallas",
                    "Prepara tu mente para dormir",
                    "Desconecta lentamente",
                    "Calma tu cuerpo antes de dormir"
                ))
                else -> null
            }

            "Tocar un instrumento" -> when (moment) {
                "Al despertar" -> randomMission(listOf(
                    "Toca tu instrumento durante $minutes minutos",
                    "Improvisa libremente",
                    "Practica antes del celular",
                    "Explora sonidos nuevos",
                    "Toca algo sencillo",
                    "Activa tu creatividad tocando"
                ))
                "Estudiar / trabajar" -> randomMission(listOf(
                    "Practica sin interrupciones",
                    "Repite una parte de una canción",
                    "Desbloquea tu mente tocando",
                    "Practica técnica básica",
                    "Toca para despejarte",
                    "Haz práctica consciente"
                ))
                "Tiempo libre" -> randomMission(listOf(
                    "Toca tu canción favorita",
                    "Improvisa sin parar",
                    "Aprende algo nuevo",
                    "Practica escalas",
                    "Explora creatividad musical",
                    "Toca sin presión"
                ))
                "Antes de dormir" -> randomMission(listOf(
                    "Toca algo tranquilo",
                    "Relájate tocando",
                    "Haz música suave",
                    "Cierra el día tocando",
                    "Improvisa lentamente",
                    "Desconecta con música"
                ))
                else -> null
            }

            "Cocinar" -> when (moment) {
                "Al despertar" -> randomMission(listOf(
                    "Prepara tu desayuno sin celular",
                    "Cocina algo sencillo",
                    "Prepara tu bebida favorita",
                    "Empieza el día cocinando",
                    "Haz algo nutritivo",
                    "Disfruta cocinar con calma"
                ))
                "Estudiar / trabajar" -> randomMission(listOf(
                    "Haz un snack sin celular",
                    "Recarga energía cocinando",
                    "Prepara algo rápido",
                    "Haz una pausa cocinando",
                    "Cocina algo simple",
                    "Aliméntate conscientemente"
                ))
                "Tiempo libre" -> randomMission(listOf(
                    "Prepara algo sin distracciones",
                    "Aprende una receta nueva",
                    "Experimenta en cocina",
                    "Cocina con calma",
                    "Crea algo diferente",
                    "Disfruta el proceso"
                ))
                "Antes de dormir" -> randomMission(listOf(
                    "Prepara algo ligero",
                    "Relájate cocinando",
                    "Haz algo simple",
                    "Cierra el día en cocina",
                    "Prepara bebida caliente",
                    "Cocina sin prisa"
                ))
                else -> null
            }

            "Aprender un idioma" -> when (moment) {
                "Al despertar" -> randomMission(listOf(
                    "Aprende 3 palabras nuevas",
                    "Practica frases durante $minutes minutos",
                    "Repasa palabras",
                    "Pronuncia en voz alta",
                    "Escucha frases cortas",
                    "Activa tu mente con idioma"
                ))
                "Estudiar / trabajar" -> randomMission(listOf(
                    "Practica sin interrupciones",
                    "Repite frases en voz alta",
                    "Aprende algo nuevo",
                    "Haz ejercicios cortos",
                    "Refuerza vocabulario",
                    "Practica comprensión"
                ))
                "Tiempo libre" -> randomMission(listOf(
                    "Usa una app durante $minutes minutos",
                    "Descubre palabras nuevas",
                    "Escucha y entiende",
                    "Mira contenido en otro idioma",
                    "Practica pronunciación",
                    "Aprende jugando"
                ))
                "Antes de dormir" -> randomMission(listOf(
                    "Repasa lo aprendido",
                    "Escucha frases",
                    "Aprende algo ligero",
                    "Refuerza vocabulario",
                    "Haz práctica suave",
                    "Cierra el día aprendiendo"
                ))
                else -> null
            }

            "Escribir" -> when (moment) {
                "Al despertar" -> randomMission(listOf(
                    "Escribe libremente durante $minutes minutos",
                    "Haz journaling",
                    "Empieza el día escribiendo",
                    "Escribe sin pensar",
                    "Plasma tus ideas",
                    "Escribe lo que sientes"
                ))
                "Estudiar / trabajar" -> randomMission(listOf(
                    "Escribe sin distracciones",
                    "Organiza ideas",
                    "Haz una pausa escribiendo",
                    "Escribe algo creativo",
                    "Desarrolla una idea",
                    "Expresa pensamientos"
                ))
                "Tiempo libre" -> randomMission(listOf(
                    "Escribe una historia",
                    "Deja fluir ideas",
                    "Crea contenido",
                    "Escribe sin parar",
                    "Explora creatividad",
                    "Describe algo de tu día"
                ))
                "Antes de dormir" -> randomMission(listOf(
                    "Escribe cómo fue tu día",
                    "Haz journaling nocturno",
                    "Reflexiona escribiendo",
                    "Cierra el día escribiendo",
                    "Escribe algo breve",
                    "Ordena tus pensamientos"
                ))
                else -> null
            }

            "Programar" -> when (moment) {
                "Al despertar" -> randomMission(listOf(
                    "Resuelve un problema simple",
                    "Escribe código sin distracciones",
                    "Aprende algo nuevo",
                    "Revisa conceptos",
                    "Haz práctica corta",
                    "Activa tu lógica"
                ))
                "Estudiar / trabajar" -> randomMission(listOf(
                    "Programa en enfoque total",
                    "Avanza en un proyecto",
                    "Practica algoritmos",
                    "Refactoriza código",
                    "Resuelve un bug",
                    "Haz coding challenge"
                ))
                "Tiempo libre" -> randomMission(listOf(
                    "Construye algo pequeño",
                    "Explora tecnología nueva",
                    "Practica código",
                    "Crea un mini proyecto",
                    "Aprende una herramienta",
                    "Experimenta programando"
                ))
                "Antes de dormir" -> randomMission(listOf(
                    "Revisa código",
                    "Aprende algo ligero",
                    "Repasa conceptos",
                    "Haz práctica corta",
                    "Lee sobre programación",
                    "Refuerza conocimiento"
                ))
                else -> null
            }

            "Dibujar" -> when (moment) {
                "Al despertar" -> randomMission(listOf(
                    "Haz un dibujo rápido",
                    "Dibuja sin pensar",
                    "Haz un boceto",
                    "Expresa algo simple",
                    "Dibuja libremente",
                    "Activa tu creatividad"
                ))
                "Estudiar / trabajar" -> randomMission(listOf(
                    "Haz un sketch rápido",
                    "Dibuja sin distracciones",
                    "Haz una pausa creativa",
                    "Dibuja algo simple",
                    "Libera mente dibujando",
                    "Haz trazos libres"
                ))
                "Tiempo libre" -> randomMission(listOf(
                    "Dibuja lo que quieras",
                    "Practica técnica nueva",
                    "Crea un dibujo completo",
                    "Haz varios bocetos",
                    "Experimenta estilos",
                    "Dibuja algo real"
                ))
                "Antes de dormir" -> randomMission(listOf(
                    "Haz un dibujo relajante",
                    "Dibuja algo simple",
                    "Expresa tu día",
                    "Relájate dibujando",
                    "Haz trazos suaves",
                    "Cierra el día creando"
                ))
                else -> null
            }

            else -> null
        }
    }
}