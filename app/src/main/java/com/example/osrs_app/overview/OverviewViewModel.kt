package com.example.osrs_app.overview


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osrs_app.itemMods.calculateROI
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

    //make a combined list of items class
    private val _combinedList = MutableLiveData<List<CombinedItem>>()
    val combinedList: LiveData<List<CombinedItem>> = _combinedList

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

    //rewrite the below so it saves the combined list to the above new variable
    fun combineLatestAndMappingData() {
        val latestDataValue = _latestData.value
        val mappingInfoValue = _mappingInfo.value

        if (latestDataValue != null && mappingInfoValue != null) {
            val combinedList = latestDataValue.data.keys.mapNotNull { itemId ->
                val item = latestDataValue.data[itemId]
                val mappingData = mappingInfoValue.find { mappingItem ->
                    mappingItem.id == itemId.toInt()
                }
                if (item != null && mappingData != null) {
                    CombinedItem(
                        itemId,
                        item.high,
                        item.low,
                        item.high?.minus(item.low ?: 0),
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

            // Step 6: Update the LiveData
            _combinedList.value = combinedList
        }
    }
}