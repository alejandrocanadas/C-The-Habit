package com.example.cthehabit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cthehabit.data.repositories.FirestoreRepository
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.ui.screens.*
import com.example.cthehabit.utils.*
import com.example.cthehabit.viewmodels.AppUsageViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    usageViewModel: AppUsageViewModel
) {
    val startRoute = remember {
        if (authViewModel.isLoggedIn.value) "main" else "Inicio"
    }

    val generatedMissions = remember { mutableStateOf<List<Mission>>(emptyList()) }
    val firestoreRepository = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {

        composable("Inicio") {
            PantallaInicio(
                onEmpezarClick = { navController.navigate("registro") },
                onLoginClick = { navController.navigate("login") }
            )
        }

        composable("registro") {
            PantallaRegistro(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate("login") },
                onRegistroExitoso = {
                    navController.navigate("encuesta") {
                        popUpTo("Inicio") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            PantallaLogin(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onRegistro = { navController.navigate("registro") },
                onLoginExitoso = {
                    navController.navigate("main") {
                        popUpTo("Inicio") { inclusive = true }
                    }
                }
            )
        }

        composable("encuesta") {
            PantallaInicialEncuesta(
                onContinuar = { navController.navigate("preguntas") }
            )
        }

        composable("preguntas") {
            PantallaPreguntas(
                onFinish = { respuestas ->
                    val questionnaireMap = mapOf(
                        "q1" to (respuestas[0] ?: emptyList()),
                        "q2" to (respuestas[1] ?: emptyList()),
                        "q3" to (respuestas[2] ?: emptyList())
                    )

                    val hoursAnswer = questionnaireMap["q1"]?.firstOrNull().orEmpty()
                    val momentAnswer = questionnaireMap["q2"]?.firstOrNull().orEmpty()
                    val selectedActivities = questionnaireMap["q3"] ?: emptyList()

                    generatedMissions.value = MissionGenerator.generateMissions(
                        hoursAnswer = hoursAnswer,
                        momentAnswer = momentAnswer,
                        selectedActivities = selectedActivities
                    )

                    scope.launch {
                        firestoreRepository.saveQuestionnaire(questionnaireMap)
                    }

                    navController.navigate("misiones_iniciales")
                }
            )
        }

        composable("misiones_iniciales") {
            PantallaInicialMisiones(
                missions = generatedMissions.value,
                onContinuar = {
                    scope.launch {
                        val today = getTodayDate()
                        val missionsToSave = MissionMapper.toUserMissions(
                            missions = generatedMissions.value,
                            dateAssigned = today
                        )
                        firestoreRepository.saveMissions(missionsToSave)
                        navController.navigate("main") {
                            popUpTo("encuesta") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("main") {
            BottomNavScreen(
                authViewModel = authViewModel,
                usageViewModel = usageViewModel,
                onJugarClick = { horas ->
                    navController.navigate("batalla_misiones/$horas/0/0")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("misiones") {
            PantallaPrincipalMisiones(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "characterSelect/{horas}/{enemy}",
            arguments = listOf(
                navArgument("horas") { type = NavType.IntType },
                navArgument("enemy") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val horas = backStackEntry.arguments?.getInt("horas") ?: 0
            val enemy = backStackEntry.arguments?.getInt("enemy") ?: 0

            CharacterSelectScreen(
                horas = horas,
                onStartGame = { pIdx, _ ->
                    // Reemplaza el juego actual y limpia la selección de la pila
                    navController.navigate("batalla_misiones/$horas/$pIdx/$enemy") {
                        popUpTo("main") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "batalla_misiones/{horas}/{player}/{enemy}",
            arguments = listOf(
                navArgument("horas") { type = NavType.IntType },
                navArgument("player") { type = NavType.IntType },
                navArgument("enemy") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val horas = backStackEntry.arguments?.getInt("horas") ?: 0
            val player = backStackEntry.arguments?.getInt("player") ?: 0
            val enemy = backStackEntry.arguments?.getInt("enemy") ?: 0

            PantallaBatallaMisiones(
                horas = horas,
                playerIndex = player,
                enemyIndex = enemy,
                onBack = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onOpenCharacterSelect = {
                    navController.navigate("characterSelect/$horas/$enemy")
                },
                onOpenTrophies = {
                    navController.navigate("main")
                }
            )
        }

        composable(
            route = "game/{horas}/{player}/{enemy}",
            arguments = listOf(
                navArgument("horas") { type = NavType.IntType },
                navArgument("player") { type = NavType.IntType },
                navArgument("enemy") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val horas = backStackEntry.arguments?.getInt("horas") ?: 0
            val player = backStackEntry.arguments?.getInt("player") ?: 0
            val enemy = backStackEntry.arguments?.getInt("enemy") ?: 0

            GameScreen(
                horas = horas,
                playerIndex = player,
                enemyIndex = enemy,
                onSiguienteClick = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onOpenCharacterSelect = {
                    navController.navigate("characterSelect/$horas/$enemy")
                },
                onOpenTrophies = {
                    navController.navigate("main")
                },
                onBackToMain = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}