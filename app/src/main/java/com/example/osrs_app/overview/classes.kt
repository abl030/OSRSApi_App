package com.example.osrs_app.overview

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
    val low: Int?,
    val priceDifference: Int?,
    val roi: Double,
    val name: String?,
    val examine: String?,
    val icon: String?,
    val limit: Int?
)

// Data class to hold item ID and price difference
data class ItemPriceDifference(
    val itemId: String,
    val high: Int,
    val low: Int,
    val priceDifference: Int
)