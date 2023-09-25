package com.example.osrs_app


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.osrs_app.network.OSRSApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.osrs_app.Overview.OverviewViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var textView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        textView2 = findViewById(R.id.textView2)

        // Create an instance of the ViewModel
        val viewModel = ViewModelProvider(this).get(OverviewViewModel::class.java)
        val itemIdToFind = 9297 // Replace with the desired item ID


        // Make the Retrofit API request and update the UI
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val latestData = OSRSApi.retrofitService.getLatestPriceData()
                val mappingInfo = OSRSApi.retrofitService.getMappingData()

                // Find the item in latestData by its ID
                val item = latestData.data[itemIdToFind.toString()]

                // Find the item in mappingInfo by its ID
                val itemFromMapping = mappingInfo.find { it.id == itemIdToFind }
                val itemname = itemFromMapping?.name

                textView2.text = itemname

                // Update the UI on the main thread
                withContext(Dispatchers.Main) {
                    if (item != null) {
                        val text = "High: ${item.high}, Low: ${item.low}"
                        textView.text = text
                    } else {
                        textView.text = "OSRS data not found"
                    }
                }
            } catch (e: Exception) {
                // Handle any exceptions here
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    textView.text = "Error: ${e.message}"
                }
            }
        }
    }
}


