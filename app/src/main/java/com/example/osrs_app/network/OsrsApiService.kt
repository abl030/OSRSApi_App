package com.example.osrs_app.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://prices.runescape.wiki/api/v1/osrs/"

/**
 * Build the Moshi object with Kotlin adapter factory that Retrofit will be using.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * The Retrofit object with the Moshi converter.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

/**
 * The actual API itself, exposing two functions (at the moment)
 * to get the latest price data and the latest mapping data
 */
interface OSRSApiService {
    /**
     * Returns the latest OSRS data, and this method should be called from a Coroutine.
     */
    @GET("latest")
    suspend fun getLatestPriceData(): OSRSLatestPriceData
    /**
     * Returns the OSRS item mapping data as a list of mapping data objects
     */
    @GET("mapping")
    suspend fun getMappingData(): List<MappingData>
}

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

// Data class to hold item ID and price difference
data class ItemPriceDifference(
    val itemId: String,
    val high: Int,
    val low: Int,
    val priceDifference: Int
)


    /**
     * A public Api object that exposes the lazy-initialized Retrofit service
     */
    object OSRSApi {
        val retrofitService: OSRSApiService by lazy { retrofit.create(OSRSApiService::class.java) }
    }

