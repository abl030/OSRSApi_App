package com.example.osrs_app.Overview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osrs_app.network.OSRSApi
import kotlinx.coroutines.launch

/**
 * Based upon https://github.com/google-developer-training/android-basics-kotlin-mars-photos-app/tree/main
 * I did the course to work out how to do viewmodels. Networking cannot be called from activity main, so I had to figure this out.
 */
class OverviewViewModel : ViewModel() {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<String>()

    // The external immutable LiveData for the request status
    val status: LiveData<String> = _status

    /**
     * Call getOSRSLatestPriceData() on init so we can display status immediately.
     */
    init {
        getOSRSLatestPriceData()
        getOSRSLatestMappingData()
    }

    private fun getOSRSLatestMappingData() {
        viewModelScope.launch {
            try {
                val MappingData = OSRSApi.retrofitService.getMappingData()


            } catch (e: Exception) {
                _status.value = "Failure: ${e.message}"
            }
        }
    }
    /**
     * Gets the latest OSRS data from the OSRS API Retrofit service and updates the
     * [OSRSLatestData] [LiveData].
     */
    private fun getOSRSLatestPriceData() {
        viewModelScope.launch {
            try {
                val latestData = OSRSApi.retrofitService.getLatestPriceData()
                val item = latestData.data["2"] // You can change the key as needed
                if (item != null) {
                    _status.value = "Success: OSRS data retrieved - High: ${item.high}, Low: ${item.low}"
                } else {
                    _status.value = "Failure: OSRS data not found"
                }
            } catch (e: Exception) {
                _status.value = "Failure: ${e.message}"
            }
        }
    }
}