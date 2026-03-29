package com.example.cthehabit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.github.mikephil.charting.animation.Easing as ChartEasing
import androidx.compose.ui.window.DialogProperties
import com.example.cthehabit.data.repositories.*
import com.example.cthehabit.utils.SocialApps
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

// ─── Paleta neon-dark ────────────────────────────────────────────────────────
private val BgDark       = Color(0xFF0D0F14)
private val Surface1     = Color(0xFF161A22)
private val Surface2     = Color(0xFF1E2330)
private val Border       = Color(0xFF2A3045)
private val NeonBlue     = Color(0xFF4FC3F7)
private val NeonPurple   = Color(0xFFB57BEE)
private val NeonGreen    = Color(0xFF57EFA5)
private val NeonAmber    = Color(0xFFFFCC44)
private val NeonPink     = Color(0xFFFF6B9D)
private val TextPrimary  = Color(0xFFF0F2FF)
private val TextMuted    = Color(0xFF8892B0)

private val ChartColors = listOf(
    android.graphics.Color.parseColor("#4FC3F7"),
    android.graphics.Color.parseColor("#B57BEE"),
    android.graphics.Color.parseColor("#57EFA5"),
    android.graphics.Color.parseColor("#FFCC44"),
    android.graphics.Color.parseColor("#FF6B9D"),
    android.graphics.Color.parseColor("#F87171"),
    android.graphics.Color.parseColor("#34D399"),
    android.graphics.Color.parseColor("#A78BFA"),
    android.graphics.Color.parseColor("#FCD34D"),
    android.graphics.Color.parseColor("#60A5FA"),
)

// ─── Pantalla principal ───────────────────────────────────────────────────────
@Composable
fun PantallaGraficas(tipo: String) {
    val context = LocalContext.current

    val apps         = if (tipo == "24h") getUsageLast24h(context) else getUsageLast7Days(context)
    val usageByApp   = getUsageByApp(apps)
    val dailyUsage   = getDailyUsage(apps)

    // Mapeo a nombres amigables
    val usageByAppFriendly: Map<String, Float> = usageByApp
        .mapKeys { SocialApps.getAppName(it.key) ?: it.key }
        .entries
        .groupBy { it.key }
        .mapValues { e -> e.value.sumOf { it.value.toDouble() }.toFloat() }
        .toList()
        .sortedByDescending { it.second }
        .toMap()

    val dailyUsageFriendly: Map<String, Float> = dailyUsage.toSortedMap()

    val totalMinutes = usageByAppFriendly.values.sum()
    val topApp       = usageByAppFriendly.entries.firstOrNull()

    var expandedChart by remember { mutableStateOf<@Composable (() -> Unit)?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Fondo decorativo: puntos en grid
        GridDotBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {

            // ── Header ───────────────────────────────────────────────────────
            HeaderSection(tipo)

            Spacer(Modifier.height(20.dp))

            // ── Stat cards ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label    = "TIEMPO TOTAL",
                    value    = formatMinutes(totalMinutes),
                    accent   = NeonBlue
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label    = "APP TOP",
                    value    = topApp?.key?.take(10) ?: "—",
                    accent   = NeonPurple
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label    = "APPS",
                    value    = "${usageByAppFriendly.size}",
                    accent   = NeonGreen
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Pie chart ────────────────────────────────────────────────────
            ChartCard(
                title    = "Distribución por app",
                subtitle = "Participación relativa del tiempo",
                accent   = NeonPurple,
                onClick  = { expandedChart = { ExpandedPie(usageByAppFriendly) } }
            ) {
                NeonPieChart(usageByAppFriendly)
            }

            Spacer(Modifier.height(16.dp))

            // ── Bar chart ────────────────────────────────────────────────────
            ChartCard(
                title    = "Tiempo por aplicación",
                subtitle = "Minutos de uso acumulados",
                accent   = NeonBlue,
                onClick  = { expandedChart = { ExpandedBar(usageByAppFriendly) } }
            ) {
                NeonBarChart(usageByAppFriendly)
            }

            // ── Line chart (solo 7d) ─────────────────────────────────────────
            if (tipo == "7d") {
                Spacer(Modifier.height(16.dp))
                ChartCard(
                    title    = "Tendencia diaria",
                    subtitle = "Minutos totales por día",
                    accent   = NeonGreen,
                    onClick  = { expandedChart = { ExpandedLine(dailyUsageFriendly) } }
                ) {
                    NeonLineChart(dailyUsageFriendly)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── Dialog expandido ─────────────────────────────────────────────────────
    if (expandedChart != null) {
        Dialog(
            onDismissRequest = { expandedChart = null },
            properties       = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark.copy(alpha = 0.96f))
                    .clickable { expandedChart = null },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface1)
                        .border(1.dp, Border, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .clickable(enabled = false) {}
                ) {
                    expandedChart?.invoke()
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────
@Composable
private fun HeaderSection(tipo: String) {
    val isToday  = tipo == "24h"
    val tag      = if (isToday) "ÚLTIMAS 24H" else "ÚLTIMOS 7 DÍAS"
    val tagColor = if (isToday) NeonAmber else NeonBlue

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Barra vertical de acento
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(listOf(tagColor, NeonPurple))
                )
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text       = "Análisis de uso",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
                letterSpacing = 0.5.sp
            )
            Text(
                text     = tag,
                fontSize = 12.sp,
                color    = tagColor,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}

// ─── Stat card ───────────────────────────────────────────────────────────────
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Surface1)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            // Reducimos padding horizontal de 12 a 8 para ganar espacio
            .padding(horizontal = 8.dp, vertical = 14.dp)
    ) {
        // Borde superior de acento
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, accent, Color.Transparent)
                    )
                )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp, // Un poco menos de spacing para que no ocupe tanto
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))

            // EL CAMBIO ESTÁ AQUÍ:
            Text(
                text = value,
                fontSize = 16.sp, // Bajamos de 18 a 16 para mayor compatibilidad
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                // Si el texto es más largo que el espacio, se deslizará solo
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    initialDelayMillis = 2000,
                    velocity = 30.dp // Velocidad suave
                )
            )
        }
    }
}

// ─── Contenedor de gráfico ────────────────────────────────────────────────────
@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface1)
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        // Resplandor sutil de acento arriba-izquierda
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.08f), Color.Transparent),
                        radius = 200f
                    )
                )
        )

        Column(modifier = Modifier.padding(18.dp)) {
            // Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text       = title,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary
                    )
                    Text(
                        text     = subtitle,
                        fontSize = 11.sp,
                        color    = TextMuted
                    )
                }
                // Chip "expandir"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(accent.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text     = "↗",
                        color    = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Línea divisora con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(accent.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )

            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}

// ─── Pie chart con leyenda neon ───────────────────────────────────────────────
@Composable
fun NeonPieChart(data: Map<String, Float>, heightDp: Int = 260) {
    val colors = data.keys.mapIndexed { i, _ -> ChartColors[i % ChartColors.size] }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            factory = { ctx ->
                PieChart(ctx).apply {
                    val entries = data.map { PieEntry(it.value, "") }
                    val ds = PieDataSet(entries, "").apply {
                        this.colors    = colors
                        sliceSpace     = 3f
                        selectionShift = 10f
                    }
                    val pd = PieData(ds).apply { setDrawValues(false) }
                    this.data = pd
                    setUsePercentValues(false)
                    description.isEnabled  = false
                    legend.isEnabled       = false
                    setDrawEntryLabels(false)
                    setHoleColor(android.graphics.Color.TRANSPARENT)
                    setHoleRadius(52f)
                    setTransparentCircleAlpha(0)
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    animateY(900)
                }
            }
        )

        Spacer(Modifier.width(12.dp))

        // Leyenda
        Column(
            modifier = Modifier
                .weight(0.9f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val total = data.values.sum().takeIf { it > 0f } ?: 1f
            data.entries.forEachIndexed { index, (name, value) ->
                val pct = (value / total * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(colors[index % colors.size]))
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = name,
                            fontSize   = 11.sp,
                            color      = TextPrimary,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text     = "${formatMinutes(value)} · $pct%",
                            fontSize = 10.sp,
                            color    = TextMuted
                        )
                    }
                }
            }
        }
    }
}

// ─── Bar chart neon ───────────────────────────────────────────────────────────
@Composable
fun NeonBarChart(data: Map<String, Float>, heightDp: Int = 280) {
    val labels = data.keys.toList()
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp),
        factory = { ctx ->
            BarChart(ctx).apply {
                val entries = data.entries.mapIndexed { i, e -> BarEntry(i.toFloat(), e.value) }
                val ds = BarDataSet(entries, "").apply {
                    colors = data.keys.mapIndexed { i, _ -> ChartColors[i % ChartColors.size] }
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(v: Float): String =
                            if (v >= 60) "${(v / 60).toInt()}h${(v % 60).toInt()}m"
                            else "${v.toInt()}m"
                    }
                    valueTextColor = android.graphics.Color.parseColor("#F0F2FF")
                    valueTextSize  = 10f
                }
                this.data = BarData(ds).also { it.barWidth = 0.65f }

                xAxis.apply {
                    valueFormatter  = IndexAxisValueFormatter(labels)
                    position        = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    granularity     = 1f
                    setDrawGridLines(false)
                    textColor       = android.graphics.Color.parseColor("#8892B0")
                    textSize        = 9f
                    typeface        = android.graphics.Typeface.DEFAULT_BOLD
                    labelRotationAngle = -35f
                }
                axisLeft.apply {
                    setDrawGridLines(false)
                    gridColor       = android.graphics.Color.parseColor("#2A3045")
                    textColor       = android.graphics.Color.parseColor("#8892B0")
                    textSize        = 10f
                    isEnabled       = false
                }
                axisRight.isEnabled = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                description.isEnabled = false
                legend.isEnabled      = false
                setDrawValueAboveBar(true)
                animateY(1000, ChartEasing.EaseInOutQuart)
                invalidate()
            }
        }
    )
}

// ─── Line chart neon ─────────────────────────────────────────────────────────
@Composable
fun NeonLineChart(data: Map<String, Float>, heightDp: Int = 260) {
    val labels = data.keys.sorted()
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                val entries = labels.mapIndexed { i, k -> Entry(i.toFloat(), data[k] ?: 0f) }
                val ds = LineDataSet(entries, "").apply {
                    color             = android.graphics.Color.parseColor("#4FC3F7")
                    setCircleColor(android.graphics.Color.parseColor("#B57BEE"))
                    circleRadius      = 5f
                    circleHoleRadius  = 2.5f
                    lineWidth          = 2.5f

                    // --- EL CAMBIO CRUCIAL ESTÁ AQUÍ ---
                    setDrawValues(false) // Esto quita los números (12.1, 0.9, etc.)
                    // -----------------------------------

                    mode              = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(true)
                    fillAlpha         = 40
                    fillColor         = android.graphics.Color.parseColor("#4FC3F7")
                }

                this.data = LineData(ds)

                xAxis.apply {
                    valueFormatter  = IndexAxisValueFormatter(labels)
                    position        = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    granularity     = 1f
                    setDrawGridLines(false)
                    textColor       = android.graphics.Color.parseColor("#8892B0")
                    textSize        = 10f
                    typeface        = android.graphics.Typeface.DEFAULT_BOLD
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = android.graphics.Color.parseColor("#1E2330")
                    textColor = android.graphics.Color.parseColor("#8892B0")
                    textSize  = 10f
                }

                axisRight.isEnabled   = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                description.isEnabled = false
                legend.isEnabled      = false
                animateX(1000, ChartEasing.EaseInOutSine)
            }
        }
    )
}

// ─── Vistas expandidas (dentro del dialog) ────────────────────────────────────
@Composable
private fun ExpandedPie(data: Map<String, Float>) {
    Column {
        Text("Distribución por app", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text("Toca fuera para cerrar", fontSize = 11.sp, color = TextMuted)
        Spacer(Modifier.height(16.dp))
        NeonPieChart(data, heightDp = 420)
    }
}

@Composable
private fun ExpandedBar(data: Map<String, Float>) {
    Column {
        Text("Tiempo por aplicación", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text("Toca fuera para cerrar", fontSize = 11.sp, color = TextMuted)
        Spacer(Modifier.height(16.dp))
        NeonBarChart(data, heightDp = 420)
    }
}

@Composable
private fun ExpandedLine(data: Map<String, Float>) {
    Column {
        Text("Tendencia diaria", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text("Toca fuera para cerrar", fontSize = 11.sp, color = TextMuted)
        Spacer(Modifier.height(16.dp))
        NeonLineChart(data, heightDp = 420)
    }
}

// ─── Fondo con puntos decorativos ─────────────────────────────────────────────
@Composable
private fun GridDotBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue  = 0.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val step = 40.dp.toPx()
                val dotR = 1.5.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    var y = 0f
                    while (y < size.height) {
                        drawCircle(
                            color  = Color(0xFF4FC3F7).copy(alpha = alpha),
                            radius = dotR,
                            center = Offset(x, y)
                        )
                        y += step
                    }
                    x += step
                }
            }
    )
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
private fun formatMinutes(minutes: Float): String {
    val m = minutes.toInt()
    return if (m >= 60) "${m / 60}h ${m % 60}m" else "${m}m"
}