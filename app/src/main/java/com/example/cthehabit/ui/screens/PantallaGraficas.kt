package com.example.cthehabit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.cthehabit.data.repositories.getUsageStats
import com.example.cthehabit.data.repositories.getUsageStatsLast7Days
import com.example.cthehabit.utils.SocialApps
import com.example.cthehabit.utils.getDailyUsage
import com.example.cthehabit.utils.getUsageByApp
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun PantallaGraficas(tipo: String) {
    val context = LocalContext.current

    val apps = if (tipo == "24h") getUsageStats(context) else getUsageStatsLast7Days(context)
    val usageByApp = getUsageByApp(apps)
    val dailyUsage = getDailyUsage(apps)

    // ===== Mapear nombres amigables y sumar duplicados =====
    val mappedByApp: Map<String, Float> = usageByApp.mapKeys { SocialApps.getAppName(it.key) ?: it.key }
    val groupedByApp: Map<String, List<Map.Entry<String, Float>>> = mappedByApp.entries.groupBy { it.key }
    val usageByAppFriendly: Map<String, Float> = groupedByApp.mapValues { entry ->
        entry.value.sumOf { it.value.toDouble() }.toFloat()
    }

    // ===== Ordenar dailyUsage por fecha ascendente =====
    val dailyUsageFriendly: Map<String, Float> = dailyUsage.toSortedMap()

    var selectedChart by remember { mutableStateOf<@Composable (() -> Unit)?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (tipo == "24h") "Gráficas últimas 24h" else "Gráficas últimos 7 días",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedChart = { PieChartSection(usageByAppFriendly, expanded = true) } },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) { PieChartSection(usageByAppFriendly) }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedChart = { BarChartSection(usageByAppFriendly, expanded = true) } },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) { BarChartSection(usageByAppFriendly) }

        if (tipo == "7d") {
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedChart = { LineChartSection(dailyUsageFriendly, expanded = true) } },
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) { LineChartSection(dailyUsageFriendly) }
        }
    }

    if (selectedChart != null) {
        Dialog(onDismissRequest = { selectedChart = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) { selectedChart?.invoke() }
            }
        }
    }
}

@Composable
fun PieChartSection(data: Map<String, Float>, expanded: Boolean = false) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (expanded) 500.dp else 300.dp),
        factory = { context ->
            PieChart(context).apply {
                val entries = data.map { PieEntry(it.value, it.key) }
                val dataSet = PieDataSet(entries, "")
                dataSet.colors = listOf(
                    android.graphics.Color.rgb(255, 99, 132),
                    android.graphics.Color.rgb(54, 162, 235),
                    android.graphics.Color.rgb(255, 206, 86),
                    android.graphics.Color.rgb(75, 192, 192),
                    android.graphics.Color.rgb(153, 102, 255)
                )
                dataSet.sliceSpace = 2f

                val pieData = PieData(dataSet)
                pieData.setValueFormatter(object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = "${value.toInt()} min"
                })
                pieData.setValueTextColor(android.graphics.Color.BLACK) // LABELS en negro
                pieData.setValueTextSize(14f)

                this.data = pieData
                this.setUsePercentValues(false)
                this.description.isEnabled = false
                this.legend.isEnabled = true
                this.setDrawEntryLabels(true)
                this.setEntryLabelColor(android.graphics.Color.BLACK) // Nombres de apps en negro
                this.setDrawHoleEnabled(false)
                this.setDrawCenterText(false)
                this.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                this.animateY(1000)
            }
        }
    )
}

@Composable
fun BarChartSection(data: Map<String, Float>, expanded: Boolean = false) {
    val labels = data.keys.toList()
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (expanded) 500.dp else 350.dp),
        factory = { context ->
            BarChart(context).apply {
                val entries = data.entries.mapIndexed { index, entry ->
                    BarEntry(index.toFloat(), entry.value)
                }

                val dataSet = BarDataSet(entries, "")
                dataSet.colors = listOf(
                    android.graphics.Color.rgb(255, 99, 132),
                    android.graphics.Color.rgb(54, 162, 235),
                    android.graphics.Color.rgb(255, 206, 86),
                    android.graphics.Color.rgb(75, 192, 192),
                    android.graphics.Color.rgb(153, 102, 255)
                )

                // Mostrar valor encima con "min"
                dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} min"
                    }
                }
                dataSet.valueTextColor = android.graphics.Color.BLACK
                dataSet.valueTextSize = 12f
                this.setDrawValueAboveBar(true)

                this.data = BarData(dataSet)
                this.data.barWidth = 0.6f

                // Configurar eje X
                this.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                this.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                this.xAxis.granularity = 1f
                this.xAxis.setDrawGridLines(false)
                this.xAxis.textColor = android.graphics.Color.BLACK
                this.xAxis.labelRotationAngle = -30f // si quieres rotar para no solaparse

                this.axisLeft.setDrawGridLines(false)
                this.axisLeft.isEnabled = false
                this.axisRight.isEnabled = false

                this.setDrawGridBackground(false)
                this.setDrawBorders(false)
                this.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                this.description.isEnabled = false

                this.animateY(1000)
                this.invalidate()
            }
        }
    )
}
@Composable
fun LineChartSection(data: Map<String, Float>, expanded: Boolean = false) {
    val labels = data.keys.sorted() // Asegura orden cronológico
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (expanded) 500.dp else 350.dp),
        factory = { context ->
            LineChart(context).apply {
                // Crear entries en orden
                val entries = labels.mapIndexed { index, key ->
                    Entry(index.toFloat(), data[key] ?: 0f)
                }

                val dataSet = LineDataSet(entries, "Uso diario")
                dataSet.color = android.graphics.Color.rgb(54, 162, 235)
                dataSet.circleRadius = 6f
                dataSet.circleHoleRadius = 3f
                dataSet.setCircleColor(android.graphics.Color.rgb(255, 99, 132))
                dataSet.lineWidth = 3f
                dataSet.valueTextSize = 12f
                dataSet.valueTextColor = android.graphics.Color.BLACK
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                this.data = LineData(dataSet)

                // Configurar eje X
                this.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                this.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                this.xAxis.granularity = 1f
                this.xAxis.setDrawGridLines(false)
                this.xAxis.textColor = android.graphics.Color.BLACK
                this.axisLeft.setDrawGridLines(false)
                this.axisRight.isEnabled = false

                this.setDrawGridBackground(false)
                this.setDrawBorders(false)
                this.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                this.description.isEnabled = false
                this.animateX(1000)
            }
        }
    )
}