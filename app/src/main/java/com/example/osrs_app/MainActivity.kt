package com.example.osrs_app


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.osrs_app.network.ItemPriceDifference
import com.example.osrs_app.network.OSRSLatestPriceData
import com.example.osrs_app.overview.OverviewViewModel
import androidx.activity.viewModels
import com.example.osrs_app.network.MappingData


class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var textView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        textView2 = findViewById(R.id.textView2)

        // Create an instance of the ViewModel
        //specifically why this works and not val viewModel = ViewModelProvider(this)[OverviewViewModel::class.java]
        //took about 2 hours of my time and I still don't exactly understand why. Thank you stack overflow.
        val viewModel: OverviewViewModel by viewModels()

        //actually grab some data, remember to do this every view.
        viewModel.fetchLatestData()
        viewModel.fetchMappingInfo()


        // Observe changes in the latestData LiveData
        viewModel.latestData.observe(this) { latestData ->
            // Check if latestData is not null before updating
            if (latestData != null) {

                // Step 2: Filter by Time (last 24 hours)
                val currentTimeMillis =
                    System.currentTimeMillis() / 1000 // Convert to Unix epoch seconds
                val timeThreshold =
                    currentTimeMillis - (24 * 60 * 60) // Subtract 24 hours in seconds

                //Step 2a. Make sure an item has been bought and sold in last 24 hours.
                val filteredData = latestData?.data?.filter { item ->
                    (item.value.highTime ?: 0) >= timeThreshold && (item.value.lowTime
                        ?: 0) >= timeThreshold
                }

                // Step 3: Calculate Price Differences
                val itemsWithPriceDifferences = filteredData?.map { item ->
                    val high = item.value.high ?: 0
                    val low = item.value.low ?: 0
                    val priceDifference = high - low
                    ItemPriceDifference(item.key, high, low, priceDifference)
                }

                // Step 4: Sort by Price Difference (descending order)
                val sortedItems =
                    itemsWithPriceDifferences?.sortedByDescending { it.priceDifference }

                // Step 5: Get the Top 10
                val top10Items = sortedItems?.take(10)

                // Access mappingInfo data here
                viewModel.mappingInfo.observe(this) { mappingInfo ->
                    // Check if latestData is not null before updating
                    if (mappingInfo != null) {
                        val stringBuilder = StringBuilder()
                        if (top10Items != null) {
                            for ((index, item) in top10Items.withIndex()) {
                                val itemId = item.itemId
                                val itemHigh = item.high
                                val itemLow = item.low
                                val itemName =
                                    mappingInfo?.find { it.id.toString() == itemId }?.name
                                stringBuilder.append("${index + 1}. Item ID: $itemId, Name: $itemName, Price Difference:${itemHigh - itemLow}\n")
                            }
                        }

                        textView.text = stringBuilder.toString()

                    }
                }
            }
        }
    }
}





