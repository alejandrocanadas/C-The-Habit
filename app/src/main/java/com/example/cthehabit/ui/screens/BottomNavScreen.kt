package com.example.cthehabit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.viewmodels.AppUsageViewModel
import com.example.cthehabit.utils.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.example.cthehabit.R

// --- Definición de Destinos ---
sealed class BottomNavDestination(val route: String, val label: String, val icon: String) {
    object Estadisticas : BottomNavDestination("estadisticas", "Stats",      "📊")
    object Calendario   : BottomNavDestination("calendar",     "Calendario", "📅")
    object Juego        : BottomNavDestination("juego",        "Juego",      "⚔️")
    object Trofeos      : BottomNavDestination("trofeos",      "Trofeos",    "🏆")
    object Perfil       : BottomNavDestination("perfil",       "Perfil",     "👤")
}

val bottomNavItems = listOf(
    BottomNavDestination.Estadisticas,
    BottomNavDestination.Calendario,
    BottomNavDestination.Juego,
    BottomNavDestination.Trofeos,
    BottomNavDestination.Perfil
)

@Composable
fun BottomNavScreen(

    authViewModel: AuthViewModel,
    usageViewModel: AppUsageViewModel,
    onJugarClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    // Registrar auto-refresh una sola vez al montar la pantalla
    LaunchedEffect(Unit) {
        usageViewModel.observeSyncAndAutoRefresh(context)
    }

    Scaffold(
        containerColor = Color(0xFF1A1C2C),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF12141F),
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            innerNavController.navigate(dest.route) {
                                popUpTo(innerNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(dest.icon, fontSize = 20.sp) },
                        label = { Text(dest.label, style = MaterialTheme.typography.bodyLarge, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF4FC3F7),
                            selectedTextColor = Color(0xFF4FC3F7),
                            indicatorColor = Color(0xFF1F2235),
                            unselectedIconColor = Color(0xFF5A5E7A),
                            unselectedTextColor = Color(0xFF5A5E7A)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = BottomNavDestination.Juego.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // PESTAÑA 1: ESTADÍSTICAS
            composable(BottomNavDestination.Estadisticas.route) {
                SeccionEstadisticas(usageViewModel, innerNavController)
            }

            // PESTAÑA 2: CALENDARIO
            composable(BottomNavDestination.Calendario.route) {
                PantallaCalendario()
            }

            // PESTAÑA 3: JUEGO (Pantalla Principal con lógica de nivel y sync)
            composable(BottomNavDestination.Juego.route) {

                val usageData by usageViewModel.usageData

                val horasHoy = remember(usageData) {
                    val today = SimpleDateFormat("d/M", Locale.getDefault()).format(Date())
                    val todayMap = usageData[today] ?: emptyMap()
                    val totalMs = todayMap.values.sum()
                    (totalMs / (1000L * 60 * 60)).toInt()
                }

                PantallaBatallaMisiones(
                    horas = horasHoy,
                    playerIndex = 0,
                    enemyIndex = 0,
                    onBack = {},
                    onOpenCharacterSelect = {
                        innerNavController.navigate("character_select_interno/$horasHoy/0")
                    },
                    onOpenTrophies = {
                        innerNavController.navigate(BottomNavDestination.Trofeos.route)
                    },
                    onOpenMissions = {
                        innerNavController.navigate("misiones_internas")
                    }
                )
            }

            // PESTAÑA 4: TROFEOS
            composable(BottomNavDestination.Trofeos.route) {
                SalonDeTrofeos()
            }

            // PESTAÑA 5: PERFIL
            composable(BottomNavDestination.Perfil.route) {
                PantallaPerfil(
                    authViewModel = authViewModel,
                    usageViewModel = usageViewModel,
                    onLogout = onLogout
                )
            }

            // RUTAS INTERNAS (Navegación dentro del BottomBar)
            composable("graficas_internas/{tipo}") { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipo") ?: "24h"
                PantallaGraficas(tipo = tipo)
            }

            composable("misiones_internas") {
                PantallaPrincipalMisiones(onBack = { innerNavController.popBackStack() })
            }

            composable(
                route = "character_select_interno/{horas}/{enemy}",
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
                        innerNavController.navigate("batalla_misiones_interna/$horas/$pIdx/$enemy") {
                            popUpTo(BottomNavDestination.Juego.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = "batalla_misiones_interna/{horas}/{player}/{enemy}",
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
                    onBack = { innerNavController.popBackStack() },
                    onOpenCharacterSelect = {
                        innerNavController.navigate("character_select_interno/$horas/$enemy")
                    },
                    onOpenTrophies = {
                        innerNavController.navigate(BottomNavDestination.Trofeos.route)
                    },
                    onOpenMissions = {
                        innerNavController.navigate("misiones_internas") // 👈 ESTA ES LA CLAVE
                    }
                )
            }
        }
    }
}

@Composable
fun SeccionEstadisticas(usageViewModel: AppUsageViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val usageData by usageViewModel.usageData
    var modoSieteDias by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.estadisticas_uso), style = MaterialTheme.typography.titleLarge, color = Color.White, fontSize = 40.sp)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                usageViewModel.loadToday(context)
                modoSieteDias = false
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.calcular_24h), style = MaterialTheme.typography.bodyLarge) }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                usageViewModel.loadLast7Days(context)
                modoSieteDias = true
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.calcular_7dias), style = MaterialTheme.typography.bodyLarge) }

        Spacer(Modifier.height(16.dp))

        // Botones de Gráficas con Navegación arreglada
        if (usageData.isNotEmpty()) {
            val ruta = if (modoSieteDias) "graficas_internas/7d" else "graficas_internas/24h"
            Button(
                onClick = { navController.navigate(ruta) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(if (modoSieteDias)
                    stringResource(R.string.ver_graficas_7dias)
                else
                    stringResource(R.string.ver_graficas_24h),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val minutesData = mapToMinutes(usageData)
        val sortedDays = minutesData.keys.sortedWith(compareBy {
            val parts = it.split("/")
            parts[1].toInt() * 100 + parts[0].toInt()
        }).reversed()

        LazyColumn {
            sortedDays.forEach { day ->
                item {
                    Text(
                        text = formatDayLabel(day),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4FC3F7),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                val apps = minutesData[day] ?: emptyMap()
                items(apps.entries.sortedByDescending { it.value }) { (app, minutes) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2235))
                    ) {
                        Row(Modifier.padding(12.dp), Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(getSocialAppNameLabel(app) ?: app, color = Color.White, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Text(stringResource(R.string.minutos, minutes.toInt()), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

// Helpers de Formato
fun formatDayLabel(key: String): String {
    return try {
        val parts = key.split("/")
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
        }
        SimpleDateFormat("d 'de' MMMM", Locale("es", "ES")).format(calendar.time)
    } catch (e: Exception) { key }
}

fun getSocialAppNameLabel(packageName: String): String? {
    val pkg = packageName.lowercase()
    return when {
        pkg.contains("instagram") -> "Instagram"
        pkg.contains("facebook") -> "Facebook"
        pkg.contains("tiktok") || pkg.contains("musically") -> "TikTok"
        pkg.contains("twitter") || pkg.contains("x.") -> "X"
        pkg.contains("reddit") -> "Reddit"
        pkg.contains("youtube") -> "YouTube"
        pkg.contains("snapchat") -> "Snapchat"
        pkg.contains("vsco") -> "VSCO"
        pkg.contains("whatsapp") -> "WhatsApp"
        else -> null
    }
}

