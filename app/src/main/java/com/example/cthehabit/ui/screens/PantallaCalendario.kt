package com.example.cthehabit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import androidx.compose.foundation.border

// Categorías de desempeño (por ahora vacías, después las llenamos)
enum class DayStatus {
    GOOD,    // verde
    REGULAR, // amarillo
    BAD,     // rojo
    NONE     // sin datos
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

    // Mapa de días con estado (por ahora vacío, después vendrá de Firestore)
    val dayStatusMap = remember { mutableStateOf<Map<String, DayStatus>>(emptyMap()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Título del mes actual
        val visibleMonth = state.firstVisibleMonth.yearMonth
        Text(
            text = visibleMonth.month
                .getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                .replaceFirstChar { it.uppercase() } + " ${visibleMonth.year}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        // Días de la semana
        DaysOfWeekHeader(firstDayOfWeek = firstDayOfWeek)

        Spacer(modifier = Modifier.height(8.dp))

        // Calendario
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                DayCell(
                    day = day,
                    status = dayStatusMap.value[day.date.toString()] ?: DayStatus.NONE
                )
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Leyenda
        CalendarLegend()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Historial de Misiones",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )
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