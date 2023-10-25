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

//unused, changed the way the data is processed
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
//this gives you the historic highest and lowest prices. But in the end i didn't end up displaying this. Future release?
fun extractHighLowValues(modList: TimeSeriesModList): Triple<Long?, Long?, Long?> {

    val highestTimestampPriceDiff = modList.data.maxByOrNull { it.timestampPriceDiff }?.timestampPriceDiff
    val highestAvgHighPrice = modList.data.maxByOrNull { it.avgHighPrice ?: Long.MIN_VALUE }?.avgHighPrice
    val lowestAvgLowPrice = modList.data.minByOrNull { it.avgLowPrice ?: Long.MAX_VALUE }?.avgLowPrice

    return Triple(highestAvgHighPrice, lowestAvgLowPrice, highestTimestampPriceDiff)
}

//takes in a set of time-series data with an enum describing which bit of the data to use. Fills the line chart with
//the  appropriate data for the enum type.
fun updateChart(chartType: ChartType, timeSeriesData: TimeSeriesResponse?, lineChart: LineChart) {
    if (timeSeriesData != null) {

        //was originally going to filter by time, but there wasn't enough data for the low volume items
        //future versions would display the last five data points and have a button toggle to show all data points
        val filteredData = timeSeriesData.data

        val entries = ArrayList<Entry>()

        //initialise the first non null price value, these are then used to keep track of the price difference over time
        val firstNonNullHighPrice = filteredData.firstOrNull { it.avgHighPrice != null }?.avgHighPrice?.toFloat()
        val firstNonNullLowPrice = filteredData.firstOrNull { it.avgLowPrice != null }?.avgLowPrice?.toFloat()

        var lastNonNullHighPrice = firstNonNullHighPrice
        var lastNonNullLowPrice = firstNonNullLowPrice

        //iterate over every item in the list, adding the data to the chart dataset
        filteredData.forEachIndexed { _, dataItem ->

            val currentHighPrice = dataItem.avgHighPrice?.toFloat() ?: lastNonNullHighPrice
            val currentLowPrice = dataItem.avgLowPrice?.toFloat() ?: lastNonNullLowPrice

            when (chartType) {
                ChartType.HIGH_PRICE -> dataItem.avgHighPrice?.let { highPrice ->
                    // remember the ?, if the value is null, it will not be added to the list
                    entries.add(Entry(dataItem.timestamp.toFloat(), highPrice.toFloat()))
                }
                ChartType.LOW_PRICE -> dataItem.avgLowPrice?.let { lowPrice ->
                    // 'let' is only executed if 'avgLowPrice' is not null
                    entries.add(Entry(dataItem.timestamp.toFloat(), lowPrice.toFloat()))
                }
                //my now overly complicated way of getting a price gap at every point in time
                //before it would only work when there were high and low prices in every time step
                //the biggest issue is the intent passes the exact current price gap
                //where as for this chart we are using the time series average data
                //to-do would be to rewrite the whole codebase to use the time series data instead of the intent data
                ChartType.PRICE_DELTA -> {
                    val priceGap = currentHighPrice?.minus(currentLowPrice!!)
                    priceGap?.let { Entry(dataItem.timestamp.toFloat(), it) }
                        ?.let { entries.add(it) }
                }
                }
            // Updating the lastNonNullHighPrice and lastNonNullLowPrice
            if (dataItem.avgHighPrice != null) {
                lastNonNullHighPrice = currentHighPrice
            }
            if (dataItem.avgLowPrice != null) {
                lastNonNullLowPrice = currentLowPrice
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


        //remove description
        lineChart.description = null

        //this took so so long. I originally had an index as my x-axis, but that was not what I wanted. I wanted the timestamp!
        //like 2 days long! But it uses my custom value formatter to convert the timestamp to a human readable string
        xAxis.valueFormatter = DataValueFormatter()

        //set the y-axis to the large value formatter, but with an edit to change all small e's to E's.
        lineChart.axisLeft.valueFormatter = LargeValueFormatterEdit()
        lineChart.axisRight.valueFormatter = LargeValueFormatterEdit()

        //stop silly .1 gp prices for low value items. This is a bit of a hack, but it works.
        lineChart.axisLeft.granularity = 1f
        lineChart.axisRight.granularity = 1f

        //turn off labels, only need the markerView now.
        lineDataSet.setDrawValues(false)

        // Refresh the chart
        lineChart.invalidate()
    }
}


//function to work out the oldest and newest time in the list as well as the average time-step between each entry
//essentially a test function to make sure everything is working. Not used in the app - but used extensively throughout testing.
fun timeSeriesStats(timeSeriesData: TimeSeriesResponse?): Pair<String?, String?> {
    if (timeSeriesData != null) {
    val limit = 0
        val oldestTime = timeSeriesData.data.minByOrNull { it.timestamp }?.timestamp
        val newestTime = timeSeriesData.data.maxByOrNull { it.timestamp }?.timestamp
        val averageTimeStep = ((newestTime?.minus(oldestTime ?: 0))?.div(timeSeriesData.data.size)
            ?.div(60))


        val stringBuilder = StringBuilder()

        //no longer used average timestep to determine volume, because two sales could occur in one time step
        if (averageTimeStep != null && averageTimeStep > 0) {
            stringBuilder.append("Average high time step in minutes: ${averageHighTimeStepInMinutes(timeSeriesData)}. ")
            stringBuilder.append("with volume: ${highPriceVolume(timeSeriesData)}. ")
            stringBuilder.append("Average low time step in minutes: ${averageLowTimeStepInMinutes(timeSeriesData)}. ")
            stringBuilder.append("with volume: ${lowPriceVolume(timeSeriesData)}. ")

            stringBuilder.append("High to low ratio: ${hightolowratioString(timeSeriesData)}. ")
            stringBuilder.append("Potential profit per flip: ${profitPerFlipInt(timeSeriesData)?.let { kFormatter(it) } ?: "null"}. ")
            stringBuilder.append("Potential profit per hour: ${potentialProfitPerHour(timeSeriesData)?.let { kFormatter(it) } ?: "null"}. ")
            stringBuilder.append("Suggested buy offer price: ${kFormatter(suggestedBuyOfferPriceInt(timeSeriesData))}. ")
            stringBuilder.append("Suggested sell offer price: ${kFormatter(suggestedSellOfferPriceInt(timeSeriesData))}. ")
            stringBuilder.append("Suggested profit per hour: ${kFormatter(suggestedProfitPerHourInt(timeSeriesData, limit))}.")

            return Pair(" ", stringBuilder.toString())
        } else {
            //not actually used.
            return Pair("The average sale time is below the granularity of the time series data", "The dataset cannot determine volumes")
        }
    }
    return Pair(null, null)
}


//function taking in timeSeriesData and finding the number of times the high price has changed
//these two pair functions I didn't end up using, took me a while to figure out that you could have two sales occurring in
//the same time period.
fun highPriceChanges(timeSeriesData: TimeSeriesResponse?): Int {
    val highPriceChanges = timeSeriesData?.data?.count { it.avgHighPrice != null }
    return highPriceChanges ?: 0
}
//function taking in timeSeriesData and finding the number of times the low price has changed
//not used as above
fun lowPriceChanges(timeSeriesData: TimeSeriesResponse?): Int {
    val lowPriceChanges = timeSeriesData?.data?.count { it.avgLowPrice != null }
    return lowPriceChanges ?: 0
}

//this is the better way to work out volume, as it takes into account the number of sales in every time period
//takes in time series and returns an int of high price sales.
fun highPriceVolume(timeSeriesData: TimeSeriesResponse?): Int {
    val highPriceVolume = timeSeriesData?.data?.sumOf { it.highPriceVolume }
    return highPriceVolume ?: 0
}
//takes in time series and returns an int of low price sales.
fun lowPriceVolume(timeSeriesData: TimeSeriesResponse?): Int {
    val lowPriceVolume = timeSeriesData?.data?.sumOf { it.lowPriceVolume }
    return lowPriceVolume ?: 0
}
//takes in time series and returns a double the average time step between high price sales in minutes.
fun averageHighTimeStepInMinutes(timeSeriesData: TimeSeriesResponse?): Double? {
    val oldestTime = timeSeriesData?.data?.filter { it.avgHighPrice != null }?.minByOrNull { it.timestamp }?.timestamp
    val newestTime = timeSeriesData?.data?.filter { it.avgHighPrice != null }?.maxByOrNull { it.timestamp }?.timestamp
    val highPriceVolume = highPriceVolume(timeSeriesData).toDouble()

    return (newestTime?.toDouble()?.minus(oldestTime?.toDouble() ?: 0.0))?.div(highPriceVolume)?.div(60.0)
}

//takes in time series and returns a double the average time step between low price sales in minutes.
fun averageLowTimeStepInMinutes(timeSeriesData: TimeSeriesResponse?): Double? {
    val oldestTime = timeSeriesData?.data?.filter { it.avgLowPrice != null }?.minByOrNull { it.timestamp }?.timestamp
    val newestTime = timeSeriesData?.data?.filter { it.avgLowPrice != null }?.maxByOrNull { it.timestamp }?.timestamp
    val lowPriceVolume = lowPriceVolume(timeSeriesData).toDouble()

    return (newestTime?.toDouble()?.minus(oldestTime?.toDouble() ?: 0.0))?.div(lowPriceVolume)?.div(60.0)
}

//takes in time series and returns a string of the high to low ratio formatted to 2 decimal places.
fun hightolowratioString(timeSeriesData: TimeSeriesResponse?): String {
    val highVol = highPriceVolume(timeSeriesData).toDouble()
    val lowVol = lowPriceVolume(timeSeriesData).toDouble()
    val highToLowRatio = highVol / lowVol
    return String.format("%.2f", highToLowRatio)
}

//format some text for the ratio
fun formatRatio(ratio: String?): String {
    return "For every item you buy, you will be able to sell $ratio items"

}

//helpful explainers to better understand how items trade. This is for me to get my head around some of the stranger items to trade
fun ratioWarnings(ratio: String?): String {
    return when {
        ratio != null && ratio.toDouble() > 1.5 -> "This item may by hard to buy at the low price, if bought it will easily sell"
        ratio != null && ratio.toDouble() < 0.5 -> "This item will easily buy low but it will be hard to sell at the high price."
        else -> "This item has a balanced ratio, they buy and sell at similar amounts"
    }
}


//takes in the time series and returns the theoretical max profit per flip
//takes tax into account. Returns an int.
fun profitPerFlipInt(timeSeriesData: TimeSeriesResponse?): Int? {
    val recentHighPrice = timeSeriesData?.data?.filter { it.avgHighPrice != null }?.maxByOrNull { it.timestamp }?.avgHighPrice
    val recentLowPrice = timeSeriesData?.data?.filter { it.avgLowPrice != null }?.maxByOrNull { it.timestamp }?.avgLowPrice
    val taxAmount = findTaxValueTimeSeries(timeSeriesData)

    return (taxAmount.let { recentHighPrice?.minus(recentLowPrice!!)?.minus(it) })?.toInt()
}

//general function to find the tax value on the most highest high price.
//this one is just a wrapper function to allow timeseries data to be used.
fun findTaxValueTimeSeries(timeSeriesData: TimeSeriesResponse?): Int {
    val recentHighPrice = timeSeriesData?.data?.filter { it.avgHighPrice != null }?.maxByOrNull { it.timestamp }?.avgHighPrice

    return findTaxValueTimeInt(recentHighPrice?.toInt() ?: 0)

}

//the actual tax value function that takes in an int and returns an int
fun findTaxValueTimeInt(recentHighPrice: Int): Int{
    if (recentHighPrice <= 99) {
        return 0
    }

    return if (recentHighPrice > 500000000) {
        5000000
    } else {
        (recentHighPrice * 0.01).toInt()
    }
}

//takes in the timeseries, accounts for tax.
//returns an int of the potential theoretical profit per hour. Used only for testing.
fun potentialProfitPerHour(timeSeriesData: TimeSeriesResponse?): Int? {
    val avgHighPriceTime = averageHighTimeStepInMinutes(timeSeriesData)
    val avgLowPriceTime = averageLowTimeStepInMinutes(timeSeriesData)

    return if (avgHighPriceTime != null) {
        if (avgHighPriceTime >= avgLowPriceTime!!) {
            val profitPerHour =
                (profitPerFlipInt(timeSeriesData)?.div(avgHighPriceTime))?.times(60)
            profitPerHour?.toInt()
        } else {
            val profitPerHour =
                (profitPerFlipInt(timeSeriesData)?.div(avgLowPriceTime))?.times(60)
            profitPerHour?.toInt()
        }
    }
    else {
        0
    }
}

//the meaty function, takes in time series and performs some basic analysis to suggest a buy price.
//this function grew and grew as I had more ideas. really needs to be refactored.
fun suggestedBuyOfferPriceInt(timeSeriesData: TimeSeriesResponse?): Int {
    val sortedData =
        timeSeriesData?.data?.filter { it.avgLowPrice != null }?.sortedByDescending { it.timestamp }
    val recentlowPrice = timeSeriesData?.data?.filter { it.avgLowPrice != null }
        ?.maxByOrNull { it.timestamp }?.avgLowPrice
    val secondNewestLowPrice =
        sortedData?.getOrNull(1)?.avgLowPrice

    // Check if recent low price is between 1 and 5 million
    if (recentlowPrice != null && recentlowPrice > 1_000_000 && recentlowPrice < 5_000_000) {
        return recentlowPrice.toInt() + 1000
    }

    if (recentlowPrice != null && recentlowPrice <= 1_000_000 && recentlowPrice > 10_000) {
        return recentlowPrice.toInt() + 100
    }

    if (recentlowPrice != null && recentlowPrice <= 10_000 && recentlowPrice > 100) {
        return recentlowPrice.toInt() + 1
    }

    if (recentlowPrice != null && recentlowPrice <= 100) {
        return recentlowPrice.toInt()
    }

    return if (recentlowPrice != null) {
        if (recentlowPrice < secondNewestLowPrice!!) {
            (recentlowPrice + 100_000).toInt()
        } else {

            val suggestedBuyOfferPrice =
                recentlowPrice.plus(((recentlowPrice.minus(secondNewestLowPrice))))
            kotlin.math.min(suggestedBuyOfferPrice, recentlowPrice + 500_000).toInt()
        }
    } else {
        0
    }
}

//same as above using some rudimentary logic to determine a suggested sale price.
fun suggestedSellOfferPriceInt(timeSeriesData: TimeSeriesResponse?): Int {
    val sortedData = timeSeriesData?.data?.filter { it.avgHighPrice != null }
        ?.sortedByDescending { it.timestamp }
    val recentHighPrice = timeSeriesData?.data?.filter { it.avgHighPrice != null }
        ?.maxByOrNull { it.timestamp }?.avgHighPrice
    val secondNewestHighPrice = sortedData?.getOrNull(1)?.avgHighPrice

    // Check if recent high price is between 1 and 5 million
    if (recentHighPrice != null && recentHighPrice > 1_000_000 && recentHighPrice < 5_000_000) {
        return recentHighPrice.toInt() - 1000
    }

    if (recentHighPrice != null && recentHighPrice <= 1_000_000 && recentHighPrice > 10_000) {
        return recentHighPrice.toInt() - 100
    }

    if (recentHighPrice != null && recentHighPrice <= 10_000 && recentHighPrice > 100) {
        return recentHighPrice.toInt() - 1
    }

    if (recentHighPrice != null && recentHighPrice <= 100) {
        return recentHighPrice.toInt()
    }

    return if (recentHighPrice != null) {
        if (recentHighPrice > secondNewestHighPrice!!) {
            (recentHighPrice - 100_000).toInt()
        } else {
            val calculatedSellOfferPrice =
                recentHighPrice.minus(secondNewestHighPrice.minus(recentHighPrice))
            kotlin.math.max(calculatedSellOfferPrice, recentHighPrice - 500000).toInt()
        }
    } else {
        0
    }
}


//take in the time series data and 6 hour buy limit, utilises the above suggested buy/sell prices
//takes into account tax and the 6 hour buy limit to return a profit per hour int.
fun suggestedProfitPerHourInt(timeSeriesData: TimeSeriesResponse?, limit: Int): Int {
    val avgHighPriceTime = averageHighTimeStepInMinutes(timeSeriesData)
    val avgLowPriceTime = averageLowTimeStepInMinutes(timeSeriesData)
    val taxValue = findTaxValueTimeSeries(timeSeriesData)

    // Calculate profit per item with our suggested prices. Take into account tax
    val profitPerItem = (suggestedSellOfferPriceInt(timeSeriesData) - suggestedBuyOfferPriceInt(timeSeriesData)) - taxValue

    // If limit is zero, just return profit per hour without the limit modifier
    //limits are 0 for newer items in game, technically they are 'unknown'
    if (limit == 0) {
        return if (avgHighPriceTime != null) {
            if (avgHighPriceTime >= avgLowPriceTime!!) {
                val profitPerHour = profitPerItem / avgHighPriceTime * 60
                profitPerHour.toInt()
            } else {
                val profitPerHour = profitPerItem / avgLowPriceTime * 60
                profitPerHour.toInt()
            }
        } else {
            0
        }
    }

    // Calculate turnover rate based on avgHighPriceTime and avgLowPriceTime
    val turnoverRate = if (avgHighPriceTime != null && avgLowPriceTime !=null) {
        if (avgHighPriceTime >= avgLowPriceTime) {
            60.0 / avgHighPriceTime
        } else {
            60.0 / avgLowPriceTime
        }
    } else {
        0.0
    }

    // Calculate number of items you can sell within 6 hours considering the turnover rate and limit
    //just finds whichever one is smaller, the turnover rate or the limit
    val numItems = kotlin.math.min(turnoverRate * 360, limit.toDouble())

    // Calculate profit per hour with the limit modifier
    val profitPerHour = profitPerItem * numItems / 6

    return profitPerHour.toInt()
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

//format the double results from time functions
//if less than 1 minute, return seconds
// if greater than 1 minutes return just the number of minutes.
// if less than 1 second return <1 second
fun formatMinutes(double: Double?): String {
    double?.let {
        val totalSeconds = it.times(60)
        val hours = it.toInt() / 60
        val minutes = it.toInt() % 60

        return when {
            totalSeconds < 1 -> "<1 sec"
            totalSeconds < 60 -> "${String.format("%.0f", totalSeconds)} secs"
            it < 60 -> "${String.format("%.0f", it)} mins"
            else -> "${hours}hr ${minutes}m"
        }
    } ?: return "Unknown"
}




