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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
        val currentTime = System.currentTimeMillis() / 1000
        val filterTimeInSeconds = 2 * 60 * 60
        val filteredData = timeSeriesData.data.filter {
            currentTime >= filterTimeInSeconds
        }

        val entries = ArrayList<Entry>()

        // Calculate spacing for the four vertical gridlines
        val dataCount = filteredData.size
        val gridlineSpacing = dataCount / 3
        var gridlineIndex = 1

         // Adjust the format as needed
        val xLabels = ArrayList<String>()

        filteredData.forEachIndexed { index, dataItem ->
            val xValue = dataItem.timestamp.toFloat()

            // Add x-axis label for the gridlines
            if (index == gridlineIndex * gridlineSpacing) {
                val localTime = formatDate(dataItem.timestamp * 1000)
                xLabels.add(localTime)
                gridlineIndex++
            }

            when (chartType) {
                ChartType.HIGH_PRICE -> dataItem.avgHighPrice?.let { highPrice ->
                    entries.add(Entry(xValue, highPrice.toFloat()))
                }
                ChartType.LOW_PRICE -> dataItem.avgLowPrice?.let { lowPrice ->
                    entries.add(Entry(xValue, lowPrice.toFloat()))
                }
                ChartType.PRICE_DELTA -> dataItem.avgHighPrice?.let { highPrice ->
                    dataItem.avgLowPrice?.let { lowPrice ->
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

        // Customizing X-Axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.gridLineWidth = 0.5f
        xAxis.gridColor = Color.GRAY
        xAxis.labelCount = 4 // Display 4 labels evenly spaced
        xAxis.valueFormatter = IndexAxisValueFormatter(xLabels) // Set custom labels

        // Refresh the chart
        lineChart.invalidate()
    }
}


//function to work out the oldest and newest time in the list as well as the average timestep between each entry
fun timeSeriesStats(timeSeriesData: TimeSeriesResponse?): Pair<String?, String?> {
    if (timeSeriesData != null) {


        val oldestTime = timeSeriesData.data.minByOrNull { it.timestamp }?.timestamp
        val newestTime = timeSeriesData.data.maxByOrNull { it.timestamp }?.timestamp
        val averageTimeStep = (newestTime?.minus(oldestTime ?: 0))?.div(timeSeriesData.data.size)

        //val formatNew = formatDate(newestTime ?: 0)
        val formatOld = formatDate(oldestTime ?: 0)

        if (averageTimeStep != null) {
            return Pair(
                "The oldest sale in this data set is $formatOld while the average time between sales is ",
                (averageTimeStep / 60).toString() + " minutes"
            )
        }
    } else {
        return Pair(null, null)
    }
    return Pair(null, null)
}

fun formatDate(date: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(date * 1000))
}
