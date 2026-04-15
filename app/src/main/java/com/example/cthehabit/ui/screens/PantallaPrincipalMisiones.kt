package com.example.cthehabit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Scaffold
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
import com.example.cthehabit.data.entity.UserMission
import com.example.cthehabit.data.repositories.FirestoreRepository
import com.example.cthehabit.utils.getTodayDate
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import com.example.cthehabit.utils.DailyMissionPlanner
import com.example.cthehabit.utils.MissionGenerator

@Composable
fun PantallaPrincipalMisiones(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val firestoreRepository = remember { FirestoreRepository() }

    var missions by remember { mutableStateOf<List<UserMission>>(emptyList()) }

    var showXp by remember { mutableStateOf(false) }
    var xpGanada by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val today = getTodayDate()

        val todayResult = firestoreRepository.getTodayMissions(today)

        todayResult.onSuccess { loadedTodayMissions ->
            val visibleToday = loadedTodayMissions.filter { !it.completed && !it.cancelled }

            if (loadedTodayMissions.isNotEmpty()) {
                missions = visibleToday
            } else {
                val questionnaireResult = firestoreRepository.getQuestionnaire()
                val pendingResult = firestoreRepository.getPendingMissionsBefore(today)

                if (questionnaireResult.isSuccess && pendingResult.isSuccess) {
                    val questionnaire = questionnaireResult.getOrNull().orEmpty()
                    val pending = pendingResult.getOrNull().orEmpty()

                    val hoursAnswer = questionnaire["q1"]?.firstOrNull().orEmpty()
                    val momentAnswer = questionnaire["q2"]?.firstOrNull().orEmpty()
                    val selectedActivities = questionnaire["q3"] ?: emptyList()

                    val generated = MissionGenerator.generateMissions(
                        hoursAnswer = hoursAnswer,
                        momentAnswer = momentAnswer,
                        selectedActivities = selectedActivities
                    )

                    val todayPlan = DailyMissionPlanner.buildTodayMissions(
                        pending = pending,
                        generated = generated,
                        today = today
                    )

                    firestoreRepository.saveMissions(todayPlan)
                    missions = todayPlan
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Misiones de Hoy",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            missions.forEach { mission ->
                MissionCard(
                    mission = mission,
                    onComplete = {
                        scope.launch {
                            val completeResult = firestoreRepository.completeMission(mission.id)
                            val xpResult = firestoreRepository.addXpToUser(50)

                            if (completeResult.isSuccess && xpResult.isSuccess) {
                                xpGanada = 50
                                showXp = true
                                missions = missions.filter { it.id != mission.id }
                            }
                        }
                    },
                    onCancel = {
                        scope.launch {
                            firestoreRepository.cancelMission(mission.id)
                            missions = missions.filter { it.id != mission.id }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

            }

            if (missions.isEmpty()) {
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "No hay más misiones para el día de hoy",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onBack
            ) {
                Text("Volver")
            }
        }
    }

    if (showXp) {

        // Esto hace que desaparezca después de 2 segundos
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showXp = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C2C)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Text(
                    text = " +$xpGanada XP",
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp),
                    color = Color(0xFFFFD700),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MissionCard(
    mission: UserMission,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF1FA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = mission.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )

            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                IconButton(onClick = onComplete) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completar",
                        tint = Color(0xFF00A86B)
                    )
                }

                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar",
                        tint = Color.Red
                    )
                }
            }

        }
    }
}