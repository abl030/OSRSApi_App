package com.example.osrs_app.itemMods

import android.graphics.Color
import android.icu.text.SimpleDateFormat
import com.example.osrs_app.overview.TimeSeriesMod
import com.example.osrs_app.overview.TimeSeriesModList
import com.example.osrs_app.overview.TimeSeriesResponse
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
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
    //was originally going to filter by time, but there wasn't enough data for the low volume items
        val filteredData = timeSeriesData.data

        val entries = ArrayList<Entry>()


        filteredData.forEachIndexed { _, dataItem ->

            when (chartType) {
                ChartType.HIGH_PRICE -> dataItem.avgHighPrice?.let { highPrice ->
                    // remember the ? if the value is null, it will not be added to the list
                    entries.add(Entry(dataItem.timestamp.toFloat(), highPrice.toFloat()))
                }
                ChartType.LOW_PRICE -> dataItem.avgLowPrice?.let { lowPrice ->
                    // 'let' is only executed if 'avgLowPrice' is not null
                    entries.add(Entry(dataItem.timestamp.toFloat(), lowPrice.toFloat()))
                }
                ChartType.PRICE_DELTA -> dataItem.avgHighPrice?.let { highPrice ->
                    // If 'avgHighPrice' is not null, we enter this 'let'
                    dataItem.avgLowPrice?.let { lowPrice ->
                        // and we enter this one if 'avgLowPrice' is not null
                        val priceGap = highPrice - lowPrice
                        entries.add(Entry(dataItem.timestamp.toFloat(), priceGap.toFloat()))
                    }
                }
            }

        }

        // Create and format the dataset and chart
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
        lineChart.data = LineData(lineDataSet)


        // Customizing X-Axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.gridLineWidth = 0.5f
        xAxis.gridColor = Color.GRAY
        xAxis.isEnabled = true
        xAxis.labelCount = 5 // Display 3 labels evenly spaced
        xAxis.setLabelCount(5, true)

        //this took so so long. I originally had an index as my x-axis, but that was not what I wanted. I wanted the timestamp!
        xAxis.valueFormatter = DataValueFormatter()

        lineChart.axisLeft.valueFormatter = LargeValueFormatterEdit()
        lineChart.axisRight.valueFormatter = LargeValueFormatterEdit()

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
                "The oldest sale in this data set is $formatOld while the average time between sales is",
                (averageTimeStep / 60).toString() + " minutes"
            )
        }
    } else {
        return Pair(null, null)
    }
    return Pair(null, null)
}

//function to return a formatted date string
fun formatDate(date: Long): String {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return sdf.format(Date(date * 1000))
}

//convert time stamps to formatted dates in the chart
class DataValueFormatter: ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return formatDate(value.toLong())
    }
}


