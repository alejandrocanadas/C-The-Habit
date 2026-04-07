package com.example.cthehabit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.viewmodels.AppUsageViewModel
import com.example.cthehabit.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay

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

    val prefs = context.getSharedPreferences("sync_prefs", 0)
    var nextSyncTime by remember { mutableStateOf(prefs.getLong("next_sync_time", 0L)) }
    var remainingTime by remember { mutableStateOf("--") }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    LaunchedEffect(nextSyncTime) {
        while (true) {
            val diff = nextSyncTime - System.currentTimeMillis()
            remainingTime = if (diff > 0) "${diff / 60000} min" else "sincronizando..."
            delay(1000)
        }
    }

    LaunchedEffect(tienePermiso) {
        if (tienePermiso) {
            authViewModel.saveUsageEvent(context, {}, {})
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!tienePermiso) {
            Text("Debes conceder permiso de uso", color = Color.White)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { requestUsagePermission(context) }) {
                Text("Conceder permiso")
            }
            return@Column
        }

        Text("Siguiente sincronización en: $remainingTime", color = Color.Gray)
        Spacer(Modifier.height(32.dp))

        // BOTÓN JUGAR
        Button(
            onClick = { onJugarClick(5) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
        ) {
            Text("⚔️ Jugar ahora", color = Color.Black, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))

        // BOTÓN MISIONES
        Button(
            onClick = onMisionesClick,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Ver misiones", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))

        // BOTÓN REINICIAR NIVEL
        OutlinedButton(
            onClick = { mostrarConfirmacion = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
        ) {
            Text("Reiniciar nivel", style = MaterialTheme.typography.titleMedium)
        }
    }
}