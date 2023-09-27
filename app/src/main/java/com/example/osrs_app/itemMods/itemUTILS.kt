package com.example.osrs_app.itemMods

import com.example.osrs_app.overview.OSRSItem

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

