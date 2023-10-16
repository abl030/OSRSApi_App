package com.example.osrs_app.itemMods

import android.graphics.Color
import android.icu.text.SimpleDateFormat
import com.example.osrs_app.overview.TimeSeriesMod
import com.example.osrs_app.overview.TimeSeriesModList
import com.example.osrs_app.overview.TimeSeriesResponse
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale



fun transformToModList(response: TimeSeriesResponse): TimeSeriesModList {
    val modList = response.data.map { entry ->
        TimeSeriesMod(
            timestamp = entry.timestamp,
            avgHighPrice = entry.avgHighPrice,
            avgLowPrice = entry.avgLowPrice,
            highPriceVolume = entry.highPriceVolume,
            lowPriceVolume = entry.lowPriceVolume,
            timestampPriceDiff = (entry.avgHighPrice ?: 0) - (entry.avgLowPrice ?: 0)
        )
    }
    return TimeSeriesModList(data = modList)
}

fun extractHighLowValues(modList: TimeSeriesModList): Triple<Long?, Long?, Long?> {

    val highestTimestampPriceDiff = modList.data.maxByOrNull { it.timestampPriceDiff }?.timestampPriceDiff
    val highestAvgHighPrice = modList.data.maxByOrNull { it.avgHighPrice ?: Long.MIN_VALUE }?.avgHighPrice
    val lowestAvgLowPrice = modList.data.minByOrNull { it.avgLowPrice ?: Long.MAX_VALUE }?.avgLowPrice

    return Triple(highestAvgHighPrice, lowestAvgLowPrice, highestTimestampPriceDiff)
}


fun updateChart(chartType: ChartType, timeSeriesData: TimeSeriesResponse?, lineChart: LineChart) {

           if (timeSeriesData != null) {

            // Filter out data older than 12 hours
            val currentTime = System.currentTimeMillis() / 1000
            val twelveHoursInSeconds = 1 * 60 * 60
            val filteredData = timeSeriesData.data.filter {
                currentTime - it.timestamp >= twelveHoursInSeconds
            }

            val entries = ArrayList<Entry>()

            // Populate entries based on chart type
            filteredData.forEach {
                val xValue = it.timestamp.toFloat()

                when (chartType) {
                    ChartType.HIGH_PRICE -> it.avgHighPrice?.let { highPrice ->
                        entries.add(Entry(xValue, highPrice.toFloat()))
                    }
                    ChartType.LOW_PRICE -> it.avgLowPrice?.let { lowPrice ->
                        entries.add(Entry(xValue, lowPrice.toFloat()))
                    }
                    ChartType.PRICE_DELTA -> it.avgHighPrice?.let { highPrice ->
                        it.avgLowPrice?.let { lowPrice ->
                            val priceGap = highPrice - lowPrice
                            entries.add(Entry(xValue, priceGap.toFloat()))
                        }
                    }
                }
            }

            // Create and customize dataset
            val lineDataSet = LineDataSet(entries,
                chartType.name.replace('_', ' ')
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
            lineDataSet.color = when (chartType) {
                ChartType.HIGH_PRICE -> Color.BLUE
                ChartType.LOW_PRICE -> Color.GREEN
                ChartType.PRICE_DELTA -> Color.RED
            }
            lineDataSet.valueTextColor = Color.BLACK
            lineDataSet.valueTextSize = 12f

            // Set data to the LineChart
            val lineChart = lineChart
            lineChart.data = LineData(lineDataSet)

            // Customizing X-Axis to add vertical lines every 2 hours
            val xAxis = lineChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(true)
            xAxis.gridLineWidth = 0.5f
            xAxis.gridColor = Color.GRAY

            // Label the grid lines using a custom ValueFormatter
            xAxis.valueFormatter = object : ValueFormatter() {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                override fun getFormattedValue(value: Float): String {
                    val date = Date(value.toLong() * 1000)
                    val hour = Calendar.getInstance().apply { time = date }.get(Calendar.HOUR_OF_DAY)
                    return if (hour % 2 == 0) {
                        sdf.format(date)
                    } else {
                        "" // Don't label odd hours
                    }
                }
            }

            // Refresh the chart
            lineChart.invalidate()
        }
    }


