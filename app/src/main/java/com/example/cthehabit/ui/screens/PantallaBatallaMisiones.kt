package com.example.cthehabit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cthehabit.data.entity.UserMission
import com.example.cthehabit.data.repositories.FirestoreRepository
import com.example.cthehabit.utils.DailyMissionPlanner
import com.example.cthehabit.utils.MissionGenerator
import com.example.cthehabit.utils.getTodayDate
import kotlinx.coroutines.launch

@Composable
fun PantallaBatallaMisiones(
    horas: Int,
    playerIndex: Int,
    enemyIndex: Int,
    onBack: () -> Unit,
    onOpenCharacterSelect: () -> Unit,
    onOpenTrophies: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val firestoreRepository = remember { FirestoreRepository() }

    var missions by remember { mutableStateOf<List<UserMission>>(emptyList()) }

    BackHandler {
        onBack()
    }

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

    Scaffold(
        containerColor = Color.DarkGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.DarkGray)
        ) {
            BattleSection(
                horas = horas,
                playerIndex = playerIndex,
                enemyIndex = enemyIndex,
                onOpenCharacterSelect = onOpenCharacterSelect,
                onOpenTrophies = onOpenTrophies
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Misiones de Hoy",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                missions.forEach { mission ->
                    MissionCard(
                        mission = mission,
                        onComplete = {
                            scope.launch {
                                firestoreRepository.completeMission(mission.id)
                                missions = missions.filter { it.id != mission.id }
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
            }
        }
    }
}