package com.example.cthehabit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.ui.screens.*
import com.example.cthehabit.viewmodels.AppUsageViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    usageViewModel: AppUsageViewModel
) {
    val startRoute = remember {
        if (authViewModel.isLoggedIn.value) "main" else "Inicio"
    }

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

        composable("main") {
            PantallaPrincipal(
                navController = navController,
                authViewModel = authViewModel,
                usageViewModel = usageViewModel,
                onGraficas24h = { navController.navigate("graficas/24h") },
                onGraficas7d = { navController.navigate("graficas/7d") },
                onJugarClick = { horas -> navController.navigate("characterSelect/$horas") },
                onMisionesClick = { navController.navigate("misiones") },
                onTrofeosClick = { navController.navigate("trofeos") }
            )
        }

        composable("graficas/{tipo}") { backStackEntry ->
            val tipo = backStackEntry.arguments?.getString("tipo") ?: "24h"
            PantallaGraficas(tipo = tipo)
        }

        composable("trofeos") {
            SalonDeTrofeos(onBack = { navController.popBackStack() })
        }

        composable("misiones") {
            PantallaPrincipalMisiones(onBack = { navController.popBackStack() })
        }

        composable("calendar") {
            PantallaCalendario()
        }

        composable("characterSelect/{horas}") { backStackEntry ->
            val horas = backStackEntry.arguments?.getString("horas")?.toInt() ?: 0
            CharacterSelectScreen(
                horas = horas,
                onStartGame = { pIdx, eIdx ->
                    navController.navigate("game/$horas/$pIdx/$eIdx")
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
                onOpenTrophies = {
                    navController.navigate("trofeos")
                }
            )
        }
    }
}