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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.cthehabit.data.entity.AppUsage
import com.example.cthehabit.data.repositories.getUsageStats
import com.example.cthehabit.data.repositories.getUsageStatsLast7Days
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.utils.hasUsageStatsPermission
import com.example.cthehabit.utils.requestUsagePermission
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
    onGraficas24h: () -> Unit,
    onGraficas7d: () -> Unit,
    onJugarClick: (horas: Int) -> Unit,
    onMisionesClick: () -> Unit,
    onTrofeosClick: () -> Unit          // <-- nuevo parámetro
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var tienePermiso by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var apps by remember { mutableStateOf(listOf<AppUsage>()) }
    var modoSieteDias by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("sync_prefs", 0)
    var nextSyncTime by remember { mutableStateOf(prefs.getLong("next_sync_time", 0L)) }
    var remainingTime by remember { mutableStateOf("--") }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                tienePermiso = hasUsageStatsPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (tienePermiso) {
            authViewModel.saveUsageEvent(
                context = context,
                onSuccess = { },
                onError = { }
            )
        }
    }

    LaunchedEffect(nextSyncTime) {
        while (true) {
            val now = System.currentTimeMillis()
            val diff = nextSyncTime - now
            remainingTime = if (diff > 0) {
                val minutes = diff / 60000
                "$minutes min"
            } else "sincronizando..."
            delay(1000)
        }
    }

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
                        if (userId != null) {
                            val data = hashMapOf("currentLevel" to 1)
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .set(data, SetOptions.merge())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Sí, reiniciar", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!tienePermiso) {
            Text("Debes conceder permiso de uso para obtener métricas")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { requestUsagePermission(context) }) {
                Text("Conceder permiso")
            }
        } else {
            Text(
                text = "Siguiente sync en: $remainingTime",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            Button(onClick = {
                apps = getUsageStats(context)
                modoSieteDias = false
            }, modifier = Modifier.fillMaxWidth()) { Text("Obtener métricas (24h)") }

            Spacer(Modifier.height(6.dp))

            Button(onClick = {
                apps = getUsageStatsLast7Days(context)
                modoSieteDias = true
            }, modifier = Modifier.fillMaxWidth()) { Text("Obtener métricas últimos 7 días") }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onJugarClick(5) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Jugar ahora")
            }

            Spacer(Modifier.height(6.dp))

            // ── SALÓN DE TROFEOS ─────────────────────────────────────────
            Button(
                onClick = onTrofeosClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB8860B)
                )
            ) {
                Text("🏆  Salón de Trofeos", color = Color.White)
            }
            // ─────────────────────────────────────────────────────────────

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = { mostrarConfirmacion = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Reiniciar nivel", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            if (apps.isNotEmpty() && !modoSieteDias) {
                Button(onClick = onGraficas24h, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver gráficas 24h")
                }
            }

            if (apps.isNotEmpty() && modoSieteDias) {
                Button(onClick = onGraficas7d, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver gráficas 7 días")
                }
            }

            Button(
                onClick = onMisionesClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver misiones")
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = { FirebaseAuth.getInstance().signOut() }) { Text("Sign Out") }

            val headerFormat = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
            val filteredApps = apps.mapNotNull { app ->
                val name = getSocialAppName(app.packageName)
                if (name != null) app.copy(packageName = name) else null
            }
            val groupedApps = filteredApps
                .filter { it.lastTimeUsed > 0L }
                .groupBy { app ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = app.lastTimeUsed
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }.toSortedMap(compareByDescending { it })

            LazyColumn {
                groupedApps.forEach { (dayKey, appsForDay) ->
                    item {
                        Text(
                            headerFormat.format(Date(dayKey)),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(appsForDay.sortedByDescending { it.timeInForeground }) { app ->
                        val minutes = app.timeInForeground / 1000 / 60
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(app.packageName)
                                Text("$minutes min")
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
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