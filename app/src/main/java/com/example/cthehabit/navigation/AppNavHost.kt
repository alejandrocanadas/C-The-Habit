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
                    navController.navigate("main") {
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

        // --- EL CONTENEDOR CON BOTTOM BAR ---
        composable("main") {
            BottomNavScreen(
                authViewModel = authViewModel,
                usageViewModel = usageViewModel,
                onJugarClick = { horas ->
                    navController.navigate("characterSelect/$horas")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLAS DE JUEGO (PANTALLA COMPLETA FUERA DEL BOTTOM BAR) ---
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
                    navController.navigate("main") // Vuelve al main donde están los trofeos
                }
            )
        }
    }
}