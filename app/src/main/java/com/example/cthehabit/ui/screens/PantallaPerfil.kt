package com.example.cthehabit.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cthehabit.data.model.GameCharacter
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.ui.game.CharacterComponent
import com.example.cthehabit.ui.game.CharacterState
import com.example.cthehabit.viewmodels.AppUsageViewModel
import com.example.cthehabit.utils.hasUsageStatsPermission
import com.example.cthehabit.utils.requestUsagePermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.example.cthehabit.R

@Composable
fun PantallaPerfil(
    authViewModel: AuthViewModel,
    usageViewModel: AppUsageViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    var tienePermiso by remember { mutableStateOf(hasUsageStatsPermission(context)) }

    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    val playerIndex = remember { prefs.getInt("selected_player_index", 0) }
    val selectedCharacter = GameCharacter.PLAYERS.getOrElse(playerIndex) { GameCharacter.PLAYERS[0] }

    val syncPrefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    var remainingTime by remember { mutableStateOf("--") }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val next = syncPrefs.getLong("next_sync_time", 0L)
            val diff = next - System.currentTimeMillis()
            remainingTime = if (diff > 0L) "${diff / 60000} min ${(diff % 60000) / 1000} seg" else "sincronizando..."
            delay(1000)
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text(stringResource(R.string.reiniciar_nivel), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.si_se_reinicia_nivel), color = Color.LightGray) },
            containerColor = Color(0xFF1A1C2C),
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        val userId = currentUser?.uid
                        userId?.let {
                            FirebaseFirestore.getInstance().collection("users").document(it)
                                .set(mapOf("currentLevel" to 1, "xp" to 0), SetOptions.merge())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.confirmar_reinicio), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text(stringResource(R.string.cancelar), color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E14))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.perfil),
            fontSize = 18.sp,
            color = Color.Cyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1C2C), Color(0xFF0D0E14))
                    )
                )
                .border(2.dp, Color.Cyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CharacterComponent(
                modifier = Modifier
                    .size(320.dp) // Muñeco más grande
                    .offset(y = 35.dp),
                character = selectedCharacter,
                spriteState = CharacterState.IDLE
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentUser?.email ?: stringResource(R.string.sin_correo_registrado),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C2C)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF2A2D42))
        ) {
            Column(Modifier.padding(16.dp)) {
                if (!tienePermiso) {
                    Text(stringResource(R.string.faltan_permisos_de_uso), color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            requestUsagePermission(context)
                            tienePermiso = hasUsageStatsPermission(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                    ) {
                        Text(stringResource(R.string.conceder_permiso), color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    Text(stringResource(R.string.sincronizacion_activa), color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp, style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.siguiente_sincronizacion, remainingTime), color = Color.Gray, fontSize = 13.sp, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { mostrarConfirmacion = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2D42)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(stringResource(R.string.reiniciar_progreso), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(stringResource(R.string.cerrar_sesion), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}