package com.example.osrs_app.network

import com.example.osrs_app.overview.MappingData
import com.example.osrs_app.overview.OSRSLatestPriceData
import com.example.osrs_app.overview.TimeSeriesResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://prices.runescape.wiki/api/v1/osrs/"

private const val USER_AGENT = "uni_android_app - @abl030"

private val client = OkHttpClient.Builder()
    .addInterceptor(UserAgentInterceptor(USER_AGENT))
    .build()

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
    .client(client)
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

    @GET("timeseries")
    suspend fun getTimeSeriesData(
        @Query("timestep") timestep: String,
        @Query("id") itemId: Int,
        ): TimeSeriesResponse

}

/**
     * A public Api object that exposes the lazy-initialized Retrofit service
     */
    object OSRSApi {
        val retrofitService: OSRSApiService by lazy { retrofit.create(OSRSApiService::class.java) }
    }


class UserAgentInterceptor(val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }
}
