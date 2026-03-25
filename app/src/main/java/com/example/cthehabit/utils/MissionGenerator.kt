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
                "Al despertar" -> randomMission(
                    listOf(
                        "Activa tu cuerpo con estiramientos durante $minutes minutos sin parar",
                        "Muévete o estira durante $minutes minutos antes de utilizar el celular",
                        "Despierta tu cuerpo con movimientos y respiración durante $minutes minutos"
                    )
                )
                "Estudiar / trabajar" -> randomMission(
                    listOf(
                        "Levántate y camina durante $minutes minutos para descansar tu mente",
                        "Estira tu cuerpo durante $minutes minutos sin distracciones",
                        "Haz una pausa activa y mantente en movimiento durante $minutes minutos"
                    )
                )
                "Tiempo libre" -> randomMission(
                    listOf(
                        "Sal a caminar y encuentra 3 cosas nuevas en $minutes minutos",
                        "Haz actividad física durante $minutes minutos sin pausas",
                        "Escucha tu música favorita y baila durante $minutes minutos sin parar"
                    )
                )
                "Antes de dormir" -> randomMission(
                    listOf(
                        "Estira tu cuerpo durante $minutes minutos sin usar el celular",
                        "Respira profundo y suelta la tensión durante $minutes minutos",
                        "Afloja el cuerpo y relájate durante $minutes minutos"
                    )
                )
                else -> null
            }

            "Leer" -> when (moment) {
                "Al despertar" -> randomMission(
                    listOf(
                        "Lee un capítulo de tu libro favorito antes de utilizar el celular",
                        "Lee durante $minutes minutos sin interrupciones",
                        "Empieza el día leyendo algo que te guste"
                    )
                )
                "Estudiar / trabajar" -> randomMission(
                    listOf(
                        "Lee durante $minutes minutos sin distraerte",
                        "Cambia de actividad leyendo sin utilizar el celular durante $minutes minutos",
                        "Lee y encuentra una idea interesante en $minutes minutos"
                    )
                )
                "Tiempo libre" -> randomMission(
                    listOf(
                        "Lee durante $minutes minutos y concéntrate solo en la historia",
                        "Lee en silencio total durante $minutes minutos",
                        "Avanza $minutes páginas de tu libro sin distracciones"
                    )
                )
                "Antes de dormir" -> randomMission(
                    listOf(
                        "Cambia el celular por lectura durante $minutes minutos",
                        "Lee algo que te relaje",
                        "Cierra el día leyendo sin interrupciones"
                    )
                )
                else -> null
            }

            "Relajarte" -> when (moment) {
                "Al despertar" -> randomMission(
                    listOf(
                        "Toma algo caliente sin usar el celular",
                        "Respira profundo durante $minutes minutos sin distraerte",
                        "Siéntate en silencio unos minutos y conecta contigo"
                    )
                )
                "Estudiar / trabajar" -> randomMission(
                    listOf(
                        "Cierra los ojos y respira durante $minutes minutos",
                        "Aléjate de todo y haz una mini meditación sin moverte durante $minutes minutos",
                        "Toma una bebida caliente mientras descansas sin pantallas"
                    )
                )
                "Tiempo libre" -> randomMission(
                    listOf(
                        "Escucha música y relájate durante $minutes minutos sin celular",
                        "Recuéstate o siéntate cómodo sin hacer nada por $minutes minutos",
                        "Respira y desconéctate meditando durante $minutes minutos"
                    )
                )
                "Antes de dormir" -> randomMission(
                    listOf(
                        "Apaga luces fuertes y desconéctate durante $minutes minutos",
                        "Tómate algo caliente sin pantallas",
                        "Haz respiraciones profundas o medita unos minutos"
                    )
                )
                else -> null
            }

            "Tocar un instrumento" -> when (moment) {
                "Al despertar" -> randomMission(
                    listOf(
                        "Toca un instrumento durante $minutes minutos",
                        "Improvisa un rato durante $minutes minutos",
                        "Dedica tiempo a practicar antes de usar el celular"
                    )
                )

                "Estudiar / trabajar" -> randomMission(
                    listOf(
                        "Practica tu instrumrnto favorito durante $minutes minutos sin interrupciones",
                        "Repite una parte de una canción durante $minutes minutos",
                        "Desbloquea tu mente tocando sin presión"
                    )
                )

                "Tiempo libre" -> randomMission(
                    listOf(
                        "Toca tu canción favorita durante $minutes minutos",
                        "Improvisa durante $minutes minutos sin interrupciones",
                        "Aprende algo nuevo en tu instrumento durante $minutes minutos"
                    )
                )

                "Antes de dormir" -> randomMission(
                    listOf(
                        "Toca algo tranquilo durante $minutes minutos",
                        "Relájate tocando sin pensar mucho",
                        "Toca algo lento para cerrar el día"
                    )
                )
                else -> null
            }

            "Cocinar" -> when (moment) {
                "Al despertar" -> randomMission(
                    listOf(
                        "Prepara tu desayuno sin usar el celular",
                        "Cocina algo sencillo con calma",
                        "Prepara tu comida o bebida favorita"
                    )
                )

                "Estudiar / trabajar" -> randomMission(
                    listOf(
                        "Haz un snack sin usar el celular",
                        "Prepara algo para recargar energía",
                        "Cocina algo rápido mientras te tomas un break"
                    )
                )

                "Tiempo libre" -> randomMission(
                    listOf(
                        "Prepara algo durante $minutes minutos sin distracciones",
                        "Crea algo nuevo con lo que tengas",
                        "Aprende una nueva receta"
                    )
                )

                "Antes de dormir" -> randomMission(
                    listOf(
                        "Prepara algo ligero sin usar el celular",
                        "Relájate cocinando algo sencillo sin prisa",
                        "Disfruta un momento tranquilo en la cocina"
                    )
                )
                else -> null
            }

            "Aprender un idioma" -> when (moment) {
                "Al despertar" -> randomMission(
                    listOf(
                        "Aprende 3 palabras nuevas y repítelas en voz alta sin usar el celular",
                        "Practica frases durante $minutes minutos",
                        "Repasa palabras que ya hayas aprendido"
                    )
                )

                "Estudiar / trabajar" -> randomMission(
                    listOf(
                        "Practica durante $minutes minutos sin interrupciones",
                        "Repite frases en voz alta durante $minutes minutos",
                        "Aprende algo nuevo sin usar el celular"
                    )
                )

                "Tiempo libre" -> randomMission(
                    listOf(
                        "Aprende durante $minutes minutos con una app de idiomas",
                        "Descubre palabras nuevas y úsalas",
                        "Escucha y trata de entender durante X minutos"
                    )
                )

                "Antes de dormir" -> randomMission(
                    listOf(
                        "Repasa las palabras del día",
                        "Escucha frases durante X minutos",
                        "Aprende algo ligero antes de dormir"
                    )
                )
                else -> null
            }

            else -> null
        }

    }
}