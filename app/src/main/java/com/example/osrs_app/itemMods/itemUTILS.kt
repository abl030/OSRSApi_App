package com.example.osrs_app.itemMods

import com.example.osrs_app.overview.CombinedItem
import com.example.osrs_app.overview.MappingData
import com.example.osrs_app.overview.OSRSItem
import com.example.osrs_app.overview.OSRSLatestPriceData

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
                    item.low ?: 0,
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
