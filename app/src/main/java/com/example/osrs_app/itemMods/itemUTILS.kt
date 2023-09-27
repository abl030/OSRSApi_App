package com.example.osrs_app.itemMods

import com.example.osrs_app.overview.CombinedItem
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
fun combineLatestAndMappingData(latestdata: OSRSLatestPriceData, mappingData: List<MappingData>): List<CombinedItem> {
    val mappingInfoValue = mappingData

    if (latestdata != null && mappingInfoValue != null) {
        val combinedList = latestdata.data.keys.mapNotNull { itemId ->
            val item = latestdata.data[itemId]
            val mappingData = mappingInfoValue.find { mappingItem ->
                mappingItem.id == itemId.toInt()
            }
            if (item != null && mappingData != null) {
                CombinedItem(
                    itemId,
                    item.high ?: 0,
                    item.highTime ?: 0,
                    item.low ?: 0,
                    item.lowTime ?: 0,
                    item.high?.minus(item.low ?: 0) ?: 0,
                    calculateROI(item) * 100 / 100,
                    mappingData.name,
                    mappingData.examine,
                    mappingData.icon,
                    mappingData.limit
                )
            } else {
                null // Exclude items without valid data
            }
        }
        return combinedList // Return the list here
    } else {
        return emptyList() // Return an empty list if latestdata or mappingInfoValue is null
    }
}

//write a function to sort the list by any list values chosen as an input
//The list can be sorted by any value, but the default is ROI
fun sortByValue(combinedList: List<CombinedItem>, value: String = "ROI"): List<CombinedItem> {
    val sortedList = when (value) {
        "ROI" -> combinedList.sortedByDescending { it.roi }
        "Low" -> combinedList.sortedBy { it.low }
        "High" -> combinedList.sortedBy { it.high }
        "Price Difference" -> combinedList.sortedByDescending { it.priceDifference }
        else -> combinedList.sortedByDescending { it.roi }
    }
    return sortedList
}


//A function to sort the Combined list by time, Low and High time is in seconds since epoch
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
    val top10List = combinedList.take(10)
    return top10List
}