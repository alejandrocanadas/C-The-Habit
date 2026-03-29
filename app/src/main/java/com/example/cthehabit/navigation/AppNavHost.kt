package com.example.cthehabit.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.ui.screens.*
import com.example.cthehabit.ui.game.GameActivity
import androidx.compose.runtime.mutableStateOf
import com.example.cthehabit.data.repositories.FirestoreRepository
import com.example.cthehabit.utils.Mission
import com.example.cthehabit.utils.MissionGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.cthehabit.utils.MissionMapper
import com.example.cthehabit.utils.getTodayDate
import com.example.cthehabit.viewmodels.AppUsageViewModel


@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    usageViewModel: AppUsageViewModel // 👈 AÑADIR ESTO
) {
    val startRoute = remember {
        if (authViewModel.isLoggedIn.value) "main" else "Inicio"
    }

    val context = LocalContext.current
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

        composable("main") {
            PantallaPrincipal(
                navController = navController,
                authViewModel = authViewModel,
                usageViewModel = usageViewModel, // ✅ FIX
                onGraficas24h = { navController.navigate("graficas/24h") },
                onGraficas7d = { navController.navigate("graficas/7d") },
                onJugarClick = { horas ->
                    navController.navigate("characterSelect/$horas")
                },
                onMisionesClick = { navController.navigate("misiones") },
                onTrofeosClick = { navController.navigate("trofeos") } // ✅ OK
            )
        }

        composable("graficas/{tipo}") { backStackEntry ->
            val tipo = backStackEntry.arguments?.getString("tipo") ?: "24h"
            PantallaGraficas(tipo = tipo)
        }

        composable("trofeos") {
            SalonDeTrofeos(
                onBack = { navController.popBackStack() }
            )
        }

        composable("misiones") {
            PantallaPrincipalMisiones(
                onBack = { navController.popBackStack() }
            )
        }

        composable("characterSelect/{horas}") { backStackEntry ->
            val horas = backStackEntry.arguments?.getString("horas")?.toInt() ?: 0

            CharacterSelectScreen(
                horas = horas,
                onStartGame = { playerIndex, enemyIndex ->
                    val intent = Intent(context, GameActivity::class.java).apply {
                        putExtra("horas_redes", horas)
                        putExtra("playerIndex", playerIndex)
                        putExtra("enemyIndex", enemyIndex)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}