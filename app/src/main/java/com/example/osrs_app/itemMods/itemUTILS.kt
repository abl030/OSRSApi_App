package com.example.osrs_app.itemMods

import com.example.osrs_app.overview.CombinedItem
import com.example.osrs_app.overview.ItemPriceDifference
import com.example.osrs_app.overview.MappingData
import com.example.osrs_app.overview.OSRSItem
import com.example.osrs_app.overview.OSRSLatestPriceData

//Takes an OSRSItem as input.
//Returns the ROI (Return on Investment) as a double
//If the item has no low value, returns NaN

fun calculateROI(item: OSRSItem): Double {
    val high = item.high?: 0
    val low = item.low ?: 0

    // Ensure that the denominator (low) is not zero to avoid division by zero
    return if (low != 0) {
        // Calculate ROI as (high - low) / low * 100
        ((high - low) / low.toDouble()) * 100.0
    } else {
        // Handle the case where low is zero (ROI undefined)
        Double.NaN
    }
}

//Takes the latest price data as input and the latest mapping data.
//Returns a combined list of the item data
fun combineLatestAndMappingData(latestData: OSRSLatestPriceData, mappingData: List<MappingData>): List<CombinedItem> {

    val combinedList = latestData.data.keys.mapNotNull { itemId ->
        val item = latestData.data[itemId]
        val mappingDataTemp = mappingData.find { mappingItem ->
            mappingItem.id == itemId.toInt()
        }
        if (item != null && mappingDataTemp != null) {
            CombinedItem(
                itemId,
                item.high ?: 0,
                item.highTime ?: 0,
                item.low ?: 0,
                item.lowTime ?: 0,
                item.high?.minus(item.low ?: 0) ?: 0,
                (calculateROI(item) * 100 / 100).toInt(),
                mappingDataTemp.name,
                mappingDataTemp.examine,
                mappingDataTemp.icon,
                mappingDataTemp.limit
            )
        } else {
            null // Exclude items without valid data
        }
    }
    return combinedList // Return the list here
}

//sort the list by any list values chosen as an input
//The list can be sorted by any value, but the default is ROI
fun sortByValueDesc(combinedList: List<CombinedItem>, value: String = "ROI"): List<CombinedItem> {
    val sortedList = when (value) {
        "ROI" -> combinedList.sortedByDescending { it.roi }
        "Low" -> combinedList.sortedByDescending { it.low }
        "High" -> combinedList.sortedByDescending { it.high }
        "Price Difference" -> combinedList.sortedByDescending { it.priceDifference }
        else -> combinedList.sortedByDescending { it.roi }
    }
    return sortedList
}


//A function to sort/filter the Combined list by time, Low and High time is in seconds since epoch
//The latest high and low time need to be in the last 24 hours
fun sortByTime(combinedList: List<CombinedItem>): List<CombinedItem> {
    val currentTimeMillis = System.currentTimeMillis() / 1000 // Convert to Unix epoch seconds
    val timeThreshold = currentTimeMillis - (24 * 60 * 60) // Subtract 24 hours in seconds

    val sortedList = combinedList.filter { item ->
        (item.highTime ?: 0) >= timeThreshold && (item.lowTime ?: 0) >= timeThreshold
    }
    return sortedList
}

//function to return just the top 10 items
fun getTop10(combinedList: List<CombinedItem>): List<CombinedItem> {
    return combinedList.take(10)
}

//function to take in the combined list and remove all items with a high price of less than 10 mill
fun removeLowValueItems(combinedList: List<CombinedItem>): List<CombinedItem> {
    val filteredList = combinedList.filter { item ->
        (item.low ?: 0) >= 10000000
    }
    return filteredList
}

//takes in the price-difference int, divides it by 1000 and returns as an int (the K function)
fun kFormatter(priceDifference: Int): String {
    val absValue = kotlin.math.abs(priceDifference).toDouble()

    return when {
        absValue >= 1_000_000_000 -> {
            String.format("%.3fB", priceDifference / 1_000_000_000.0)
        }
        absValue >= 1_000_000 -> {
            String.format("%.3fM", priceDifference / 1_000_000.0)
        }
        absValue >= 1_000 -> {
            String.format("%.0fK", priceDifference / 1_000.0)
        }
        else -> {
            "$priceDifference GP"
        }
    }
}





//takes in a combined list as input and returns a new list with just item name, ROI and price difference
fun priceList(combinedList: List<CombinedItem>): List<ItemPriceDifference> {
    val priceList = List(combinedList.size) { index ->
        ItemPriceDifference(
            combinedList[index].name,
            combinedList[index].priceDifference ?: 0,
            combinedList[index].roi,
            combinedList[index].icon,
            combinedList[index].itemId,
            combinedList[index].high,
            combinedList[index].low,
            combinedList[index].limit,
            combinedList[index].examine
        )
    }
    return priceList
}

//limit formatter, some display 0 which actually means "unknown"
fun limitFormatter(limit: Int?): String {
    return if (limit == 0) {
        "Unknown"
    }
    else {
        limit.toString()
    }
}