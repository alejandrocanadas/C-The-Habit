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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.viewmodels.AppUsageViewModel
import com.example.cthehabit.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaPrincipal(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    usageViewModel: AppUsageViewModel,
    onGraficas24h: () -> Unit,
    onGraficas7d: () -> Unit,
    onJugarClick: (horas: Int) -> Unit,
    onMisionesClick: () -> Unit,
    onTrofeosClick: () -> Unit
) {

    val context = LocalContext.current

    var tienePermiso by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var modoSieteDias by remember { mutableStateOf(false) }

    val usageData by remember { usageViewModel.usageData }

    val prefs = context.getSharedPreferences("sync_prefs", 0)
    var nextSyncTime by remember { mutableStateOf(prefs.getLong("next_sync_time", 0L)) }
    var remainingTime by remember { mutableStateOf("--") }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // ⏱ contador sync
    LaunchedEffect(nextSyncTime) {
        while (true) {
            val diff = nextSyncTime - System.currentTimeMillis()
            remainingTime = if (diff > 0) "${diff / 60000} min" else "sincronizando..."
            delay(1000)
        }
    }

    // 🔐 guardar evento
    LaunchedEffect(tienePermiso) {
        if (tienePermiso) {
            authViewModel.saveUsageEvent(context, {}, {})
        }
    }

    // 🔴 diálogo reinicio
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("¿Reiniciar nivel?") },
            text = { Text("Tu progreso volverá al nivel 1. ¿Estás seguro?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        userId?.let {
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(it)
                                .set(mapOf("currentLevel" to 1), SetOptions.merge())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Sí, reiniciar", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {

        if (!tienePermiso) {
            Text("Debes conceder permiso de uso")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { requestUsagePermission(context) }) {
                Text("Conceder permiso")
            }
            return@Column
        }

        Text("Siguiente sync en: $remainingTime")
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                usageViewModel.loadToday(context)
                modoSieteDias = false
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Métricas 24h") }

        Spacer(Modifier.height(6.dp))

        Button(
            onClick = {
                usageViewModel.loadLast7Days(context)
                modoSieteDias = true
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Últimos 7 días") }

        Spacer(Modifier.height(8.dp))

        Button(onClick = { onJugarClick(5) }, modifier = Modifier.fillMaxWidth()) {
            Text("Jugar ahora")
        }

        Spacer(Modifier.height(6.dp))

        Button(
            onClick = onTrofeosClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8860B))
        ) {
            Text("🏆 Salón de Trofeos", color = Color.White)
        }

        Spacer(Modifier.height(6.dp))

        Button(
            onClick = { mostrarConfirmacion = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Reiniciar nivel", color = Color.White)
        }

        Spacer(Modifier.height(8.dp))

        if (usageData.isNotEmpty() && !modoSieteDias) {
            Button(onClick = onGraficas24h, modifier = Modifier.fillMaxWidth()) {
                Text("Ver gráficas 24h")
            }
        }

        if (usageData.isNotEmpty() && modoSieteDias) {
            Button(onClick = onGraficas7d, modifier = Modifier.fillMaxWidth()) {
                Text("Ver gráficas 7 días")
            }
        }

        Button(onClick = onMisionesClick, modifier = Modifier.fillMaxWidth()) {
            Text("Ver misiones")
        }

        Button(
            onClick = onTrofeosClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8860B))
        ) {
            Text("🏆 Salón de Trofeos", color = Color.White)
        }

        Spacer(Modifier.height(6.dp))

        Spacer(Modifier.height(12.dp))
        Button(onClick = { FirebaseAuth.getInstance().signOut() }) {
            Text("Sign Out")
        }

        Spacer(Modifier.height(16.dp))

        // 📊 PROCESAMIENTO LIMPIO
        val minutesData = mapToMinutes(usageData)

        // ordenar fechas correctamente
        val sortedDays = minutesData.keys.sortedWith(compareBy {
            val parts = it.split("/")
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            month * 100 + day
        }).reversed()

        LazyColumn {
            sortedDays.forEach { day ->

                item {
                    Text(
                        text = formatDay(day),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                val apps = minutesData[day] ?: emptyMap()

                items(apps.entries.sortedByDescending { it.value }) { (app, minutes) ->

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(getSocialAppName(app) ?: app)
                            Text("${minutes.toInt()} min")
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

fun formatDay(key: String): String {
    val parts = key.split("/")
    val day = parts[0].toInt()
    val month = parts[1].toInt()

    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.MONTH, month - 1)
    }

    val format = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
    return format.format(calendar.time)
}

fun getSocialAppName(packageName: String): String? {
    val pkg = packageName.lowercase()
    return when {
        pkg.contains("instagram") -> "Instagram"
        pkg.contains("facebook") -> "Facebook"
        pkg.contains("tiktok") || pkg.contains("musically") -> "TikTok"
        pkg.contains("twitter") || pkg.contains("x.") -> "X"
        pkg.contains("reddit") -> "Reddit"
        pkg.contains("youtube") -> "YouTube"
        pkg.contains("vsco") -> "VSCO"
        else -> null
    }
}