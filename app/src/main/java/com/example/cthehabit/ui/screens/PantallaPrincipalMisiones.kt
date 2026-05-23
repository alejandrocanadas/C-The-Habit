package com.example.cthehabit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cthehabit.R
import com.example.cthehabit.data.entity.UserMission
import com.example.cthehabit.data.repositories.FirestoreRepository
import com.example.cthehabit.ui.theme.*
import com.example.cthehabit.utils.DailyMissionPlanner
import com.example.cthehabit.utils.MissionGenerator
import com.example.cthehabit.utils.getTodayDate
import kotlinx.coroutines.launch

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(56.dp))


            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.c),
                    fontSize = 26.sp,
                    style = MaterialTheme.typography.titleLarge,
                    color = PixelWhite
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.the_habit),
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBlue,
                    letterSpacing = 3.sp
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = NavyBlue, thickness = 1.dp)
            Spacer(Modifier.height(24.dp))


            Text(
                text = stringResource(R.string.misiones_hoy),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp,
                color = Cyan,
                modifier = Modifier.fillMaxWidth()
            )


            Text(
                text = "${missions.size} PENDIENTES",
                style = MaterialTheme.typography.labelSmall,
                color = NavyBlue,
                fontSize = 9.sp,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(28.dp))


            if (missions.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_hay_misiones),
                    style = MaterialTheme.typography.labelSmall,
                    color = MidPurple,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(16.dp))
            } else {
                missions.forEach { mission ->
                    PixelMissionCard(
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
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = NavyBlue, thickness = 1.dp)
            Spacer(Modifier.height(20.dp))


            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelSurface,
                    contentColor = PixelWhite,
                ),
                border = BorderStroke(1.dp, NavyBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.volver),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(32.dp))
        }


        if (showXp) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showXp = false
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PixelSurface,
                    border = BorderStroke(1.dp, Cyan),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 36.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "+$xpGanada XP",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 28.sp,
                            color = Cyan
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "MISIÓN COMPLETADA",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkyBlue,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PixelMissionCard(
    mission: UserMission,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = PixelSurface,
        border = BorderStroke(1.dp, NavyBlue),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MISIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    color = NavyBlue,
                    fontSize = 8.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = mission.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PixelWhite,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.width(12.dp))


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                IconButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Cyan.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.completar),
                        tint = Cyan,
                        modifier = Modifier.size(18.dp)
                    )
                }


                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MidPurple.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancelar_accion),
                        tint = MidPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}