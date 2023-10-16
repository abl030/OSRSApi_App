package com.example.osrs_app.overview


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osrs_app.network.OSRSApi
import kotlinx.coroutines.launch



/**
 * Based upon https://github.com/google-developer-training/android-basics-kotlin-mars-photos-app/tree/main
 * I did the course to work out how to do view-models. Networking cannot be called from activity main, so I had to figure this out.
 */
class OverviewViewModel : ViewModel() {
    //make a price object using the api class.
    //need an immutable public access object, and a mutable private internal object we can work on
    private val _latestData = MutableLiveData<OSRSLatestPriceData>()
    val latestData: LiveData<OSRSLatestPriceData> = _latestData

    //make a mapping info object using the class from the api
    private val _mappingInfo = MutableLiveData<List<MappingData>>()
    val mappingInfo: LiveData<List<MappingData>> = _mappingInfo

    //make an error class to better handle errors as they occur.
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    //make a time series data object using the api class.
    private val _timeSeriesData = MutableLiveData<TimeSeriesResponse>()
    val timeSeriesData: LiveData<TimeSeriesResponse> = _timeSeriesData


    //calls the API to populate the latest price data
    fun fetchLatestData() {
        viewModelScope.launch {
            try {
                val data = OSRSApi.retrofitService.getLatestPriceData()
                _latestData.value = data
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    //calls the api to populate the mapping data
    fun fetchMappingInfo() {
        viewModelScope.launch {
            try {
                val data = OSRSApi.retrofitService.getMappingData()
                _mappingInfo.value = data
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    //calls the api to populate the time series data
    fun fetchTimeSeriesData(itemId: Int, timestep: String) {
        viewModelScope.launch {
            try {
                val data = OSRSApi.retrofitService.getTimeSeriesData(timestep, itemId,)
                _timeSeriesData.value = data
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

