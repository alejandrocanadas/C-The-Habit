package com.example.cthehabit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart

import androidx.compose.ui.graphics.Color

import com.example.cthehabit.data.repositories.getUsageStats
import com.example.cthehabit.data.repositories.getUsageStatsLast7Days
import com.example.cthehabit.utils.*

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate


@Composable
fun PantallaGraficas(tipo:String) {

    val context = LocalContext.current

    val apps = if(tipo=="24h")
        getUsageStats(context)
    else
        getUsageStatsLast7Days(context)

    val usageByApp = getUsageByApp(apps)
    val dailyUsage = getDailyUsage(apps)

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ){

        Text(
            text = if(tipo=="24h") "Gráficas últimas 24h"
            else "Gráficas últimos 7 días",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(20.dp))

        PieChartSection(usageByApp)

        Spacer(Modifier.height(40.dp))

        BarChartSection(usageByApp)

        if(tipo=="7d"){

            Spacer(Modifier.height(40.dp))

            LineChartSection(dailyUsage)
        }
    }
}

@Composable
fun PieChartSection(data: Map<String, Float>) {

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->

            val chart = PieChart(context)

            val entries = data.map {
                PieEntry(it.value, it.key)
            }

            val dataSet = PieDataSet(entries, "Uso de redes")

            dataSet.colors = listOf(
                ColorTemplate.MATERIAL_COLORS.toList()
            ).flatten()

            val pieData = PieData(dataSet)

            chart.data = pieData
            chart.description.isEnabled = false
            chart.animateY(1000)

            chart
        }
    )
}

@Composable
fun BarChartSection(data: Map<String, Float>) {

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        factory = { context ->

            val chart = BarChart(context)

            val entries = data.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value)
            }

            val dataSet = BarDataSet(entries, "Minutos")

            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

            chart.data = BarData(dataSet)

            chart.xAxis.valueFormatter =
                IndexAxisValueFormatter(data.keys.toList())

            chart.description.isEnabled = false
            chart.animateY(1000)

            chart
        }
    )
}

@Composable
fun LineChartSection(data: Map<String, Float>) {

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        factory = { context ->

            val chart = LineChart(context)

            val entries = data.entries.mapIndexed { index, entry ->
                Entry(index.toFloat(), entry.value)
            }

            val dataSet = LineDataSet(entries, "Uso diario")

            dataSet.color = android.graphics.Color.BLUE
            dataSet.circleRadius = 4f

            chart.data = LineData(dataSet)

            chart.xAxis.valueFormatter =
                IndexAxisValueFormatter(data.keys.toList())

            chart.description.isEnabled = false
            chart.animateX(1000)

            chart
        }
    )
}
fun randomColor():Color{

    val colors = listOf(
        Color(0xFFE91E63),
        Color(0xFF3F51B5),
        Color(0xFF009688),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFF03A9F4)
    )

    return colors.random()
}