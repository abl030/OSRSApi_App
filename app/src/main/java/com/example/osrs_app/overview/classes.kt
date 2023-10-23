package com.example.osrs_app.overview

import java.io.Serializable

/**
 * Define the data classes
 */
data class OSRSLatestPriceData(
    val data: Map<String, OSRSItem>
)

data class OSRSItem(
    val high: Int?,
    val highTime: Long?,
    val low: Int?,
    val lowTime: Long?,
    )

data class MappingData(
    val highalch: Int?,
    val members: Boolean?,
    val name: String?,
    val examine: String?,
    val id: Int?,
    val value: Int?,
    val icon: String?,
    val lowalch: Int?,
    val limit: Int?
)

// Data class to hold item ID and price difference, ROI, and mapping data
data class CombinedItem(
    val itemId: String?,
    val high: Int?,
    val highTime: Long?,
    val low: Int?,
    val lowTime: Long?,
    val priceDifference: Int?,
    val roi: Int,
    val name: String?,
    val examine: String?,
    val icon: String?,
    val limit: Int?
)

// Data class to hold item ID and price difference
data class ItemPriceDifference(
    val name: String?,
    val priceDifference: Int,
    val ROI: Int,
    val icon: String?,
    val itemId: String?,
    val highPrice: Int?,
    val lowPrice: Int?,
    val limit: Int?,
    val examine: String?
): Serializable


data class TimeSeriesResponse(
    val data: List<TimeSeriesEntry>
)

data class TimeSeriesEntry(
    val timestamp: Long,
    val avgHighPrice: Long?,
    val avgLowPrice: Long?,
    val highPriceVolume: Int,
    val lowPriceVolume: Int
)

data class TimeSeriesModList(
    val data: List<TimeSeriesMod>
)

data class TimeSeriesMod(
    val timestamp: Long,
    val avgHighPrice: Long?,
    val avgLowPrice: Long?,
    val highPriceVolume: Int,
    val lowPriceVolume: Int,
    val timestampPriceDiff: Long,
)

data class suggestPrice(
    val highPrice: Int,
    val lowPrice: Int
)