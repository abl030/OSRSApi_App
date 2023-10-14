package com.example.osrs_app


import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.osrs_app.itemMods.PriceList
import com.example.osrs_app.itemMods.combineLatestAndMappingData
import com.example.osrs_app.itemMods.removeLowValueItems
import com.example.osrs_app.itemMods.sortByTime
import com.example.osrs_app.overview.ItemPriceDifferenceAdapter
import com.example.osrs_app.overview.OverviewViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var textView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lateinit var recyclerView: RecyclerView
        lateinit var itemPriceDifferenceAdapter: ItemPriceDifferenceAdapter

        // Create an instance of the ViewModel
        //specifically why this works and not val viewModel = ViewModelProvider(this)[OverviewViewModel::class.java]
        //took about 2 hours of my time and I still don't exactly understand why. Thank you stack overflow.
        val viewModel: OverviewViewModel by viewModels()

        //actually grab some data, remember to do this every view.
        viewModel.fetchLatestData()
        viewModel.fetchMappingInfo()

        // Observe changes in the latestData LiveData
        viewModel.latestData.observe(this) { latestData ->
            if (latestData != null) {
                // Access mappingInfo data here
                viewModel.mappingInfo.observe(this) { mappingInfo ->
                    // Check if latestData is not null before updating
                    if (mappingInfo != null) {
                        // here we create our combined list from the price and mapping data
                        // then sort it by time, removing items that haven't traded in 24 hours
                        // then we remove all the low value items not worth our time.
                        var combinedlist = removeLowValueItems(sortByTime(combineLatestAndMappingData(latestData, mappingInfo)))
                        combinedlist = combinedlist.sortedByDescending { it.roi }
                        combinedlist = combinedlist.take(20)
                        val combinedlist2 = PriceList(combinedlist)

                        recyclerView = findViewById(R.id.recyclerView)
                        itemPriceDifferenceAdapter = ItemPriceDifferenceAdapter(combinedlist2) // Replace with your item list

                        recyclerView.adapter = itemPriceDifferenceAdapter
                        recyclerView.layoutManager = LinearLayoutManager(this)

                    }

                }
            }
        }
    }
}



