package com.example.cthehabit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import com.example.cthehabit.data.repositories.FirestoreRepository

enum class DayStatus {
    GOOD,
    REGULAR,
    BAD,
    NONE
}

// Modelo para agrupar misiones por día en el historial
data class DayMissionSummary(
    val date: LocalDate,
    val missions: List<MissionHistoryItem>,
    val usageHours: Double,
    val usageLimitHours: Double
)

data class MissionHistoryItem(
    val text: String,
    val completed: Boolean
)

fun getDailyUsageLimitHours(q1Answer: String): Double {
    return when (q1Answer.trim()) {
        "1 a 2 horas" -> 0.5
        "2 a 4 horas" -> 1.0
        "3 a 5 horas" -> 1.5
        "+5 horas", "Más de 5 horas" -> 2.5
        else -> 24.0
    }
}

fun millisToHours(millis: Long): Double {
    return millis / 1000.0 / 60.0 / 60.0
}

@Composable
fun PantallaCalendario() {

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(1) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val dayStatusMap = remember { mutableStateOf<Map<String, DayStatus>>(emptyMap()) }
    val weekHistory = remember { mutableStateOf<List<DayMissionSummary>>(emptyList()) }

    val firestoreRepository = remember { FirestoreRepository() }

    // Carga desde Firestore el historial de los últimos 7 días
    LaunchedEffect(Unit) {
        val today = LocalDate.now()
        val statusMap = mutableMapOf<String, DayStatus>()
        val history = mutableListOf<DayMissionSummary>()

        // 1. Leer cuestionario para sacar la meta diaria según q1
        val questionnaireResult = firestoreRepository.getQuestionnaire()
        val q1Answer = questionnaireResult.getOrNull()
            ?.get("q1")
            ?.firstOrNull()
            .orEmpty()

        val usageLimitHours = getDailyUsageLimitHours(q1Answer)

        // 2. Leer eventos de uso guardados en Firestore
        val usageEventsResult = firestoreRepository.getUsageEvents()
        val usageEvents = usageEventsResult.getOrNull().orEmpty()

        // 3. Revisar últimos 7 días
        for (i in 0..6) {
            val date = today.minusDays(i.toLong())
            val dateStr = date.toString()

            val result = firestoreRepository.getMissionsForDate(dateStr)
            result.onSuccess { missions ->
                if (missions.isEmpty()) return@onSuccess

                val completed = missions.count { it.completed }
                val total = missions.size

                val usageMillis = usageEvents[dateStr]?.values?.sum() ?: 0L
                val usageHours = millisToHours(usageMillis)

                val status = when {
                    usageHours > usageLimitHours -> DayStatus.BAD
                    completed == total -> DayStatus.GOOD
                    completed > 0 -> DayStatus.REGULAR
                    else -> DayStatus.BAD
                }

                statusMap[dateStr] = status

                history.add(
                    DayMissionSummary(
                        date = date,
                        missions = missions.map {
                            MissionHistoryItem(
                                text = it.text,
                                completed = it.completed
                            )
                        },
                        usageHours = usageHours,
                        usageLimitHours = usageLimitHours
                    )
                )
            }
        }

        dayStatusMap.value = statusMap
        weekHistory.value = history.sortedByDescending { it.date }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        val visibleMonth = state.firstVisibleMonth.yearMonth
        Text(
            text = visibleMonth.month
                .getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                .replaceFirstChar { it.uppercase() } + " ${visibleMonth.year}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        // ── Calendario con fondo blanco ──────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                DaysOfWeekHeader(firstDayOfWeek = firstDayOfWeek)

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalCalendar(
                    state = state,
                    modifier = Modifier.height(260.dp),
                    dayContent = { day ->
                        DayCell(
                            day = day,
                            status = dayStatusMap.value[day.date.toString()] ?: DayStatus.NONE
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CalendarLegend()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Título historial ─────────────────────────────────────────────
        Text(
            text = "Historial de misiones",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        // ── Tarjetas de historial semanal ────────────────────────────────
        if (weekHistory.value.isEmpty()) {
            Text(
                text = "No hay historial disponible aún",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            weekHistory.value.forEach { daySummary ->
                MissionHistoryCard(daySummary = daySummary, dayStatusMap = dayStatusMap.value)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun MissionHistoryCard(
    daySummary: DayMissionSummary,
    dayStatusMap: Map<String, DayStatus>
) {
    val dateStr = daySummary.date.toString()
    val status = dayStatusMap[dateStr] ?: DayStatus.NONE

    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val dateLabel = daySummary.date.format(formatter).replaceFirstChar { it.uppercase() }

    val (statusLabel, statusBg, statusTextColor) = when (status) {
        DayStatus.GOOD    -> Triple("Buen día",   Color(0xFFE8F5E9), Color(0xFF2E7D32))
        DayStatus.REGULAR -> Triple("Regular",    Color(0xFFFFF8E1), Color(0xFFF57F17))
        DayStatus.BAD     -> Triple("Mal día",    Color(0xFFFFEBEE), Color(0xFFB71C1C))
        DayStatus.NONE    -> Triple("Sin datos",  Color(0xFFF5F5F5), Color(0xFF757575))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Encabezado: fecha + badge de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateLabel,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Box(
                    modifier = Modifier
                        .background(color = statusBg, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Uso en redes: ${
                    String.format(Locale("es", "ES"), "%.1f", daySummary.usageHours)
                } h / Meta: ${
                    String.format(Locale("es", "ES"), "%.1f", daySummary.usageLimitHours)
                } h",
                fontSize = 12.sp,
                color = if (daySummary.usageHours > daySummary.usageLimitHours)
                    Color(0xFFB71C1C)
                else
                    Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de misiones del día
            daySummary.missions.forEach { mission ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (mission.completed) Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mission.text,
                        fontSize = 13.sp,
                        color = if (mission.completed)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun DaysOfWeekHeader(firstDayOfWeek: DayOfWeek) {
    val daysOfWeek = daysOfWeek(firstDayOfWeek)
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))
                    .replaceFirstChar { it.uppercase() },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DayCell(day: CalendarDay, status: DayStatus) {

    val today = remember { java.time.LocalDate.now() }
    val isToday = day.date == today

    val bgColor = when (status) {
        DayStatus.GOOD    -> Color(0xFF4CAF50)
        DayStatus.REGULAR -> Color(0xFFFFC107)
        DayStatus.BAD     -> Color(0xFFF44336)
        DayStatus.NONE    -> if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
    }

    val textColor = when {
        status != DayStatus.NONE -> Color.White
        isToday -> Color.White
        day.position == DayPosition.MonthDate -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .background(color = bgColor, shape = CircleShape)
            .then(
                if (isToday && status == DayStatus.NONE)
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isToday || status != DayStatus.NONE) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = Color(0xFF4CAF50), label = "Buen día")
        LegendItem(color = Color(0xFFFFC107), label = "Regular")
        LegendItem(color = Color(0xFFF44336), label = "Mal día")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(text = label, fontSize = 12.sp)
    }
}