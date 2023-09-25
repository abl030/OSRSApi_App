package com.example.osrs_app.Overview


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osrs_app.network.MappingData
import com.example.osrs_app.network.OSRSApi
import com.example.osrs_app.network.OSRSLatestPriceData
import kotlinx.coroutines.launch


/**
 * Based upon https://github.com/google-developer-training/android-basics-kotlin-mars-photos-app/tree/main
 * I did the course to work out how to do viewmodels. Networking cannot be called from activity main, so I had to figure this out.
 */
class OverviewViewModel : ViewModel() {
    //make a price object using the api class.
    //need an immutable public access class, and a mutable private internal object we can work on
    private val _latestData = MutableLiveData<OSRSLatestPriceData>()
    val latestData: LiveData<OSRSLatestPriceData> = _latestData

    //make a mapping info object using the class from the api
    private val _mappingInfo = MutableLiveData<List<MappingData>>()
    val mappingInfo: LiveData<List<MappingData>> = _mappingInfo

    //make an error class to better handle errors as they occur.
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

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
}