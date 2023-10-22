package com.example.osrs_app


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
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
import java.util.Locale



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lateinit var recyclerView: RecyclerView
        lateinit var itemPriceDifferenceAdapter: ItemPriceDifferenceAdapter
        lateinit var itemPriceDifferenceAdapter2: ItemPriceDifferenceAdapter

        // Create an instance of the ViewModel
        //specifically why this works and not val viewModel = ViewModelProvider(this)[OverviewViewModel::class.java]
        //took about 2 hours of my time and I still don't exactly understand why. Thank you stack overflow.
        val viewModel: OverviewViewModel by viewModels()

        //actually grab some data, remember to do this every view.
        viewModel.fetchLatestData()
        viewModel.fetchMappingInfo()
        //viewModel.fetchTimeSeriesData(4151.toInt(), "6h")


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
                        var searchlist = combineLatestAndMappingData(latestData, mappingInfo)
                        var combinedlist = removeLowValueItems(sortByTime(combineLatestAndMappingData(latestData, mappingInfo)))

                        //then we sort by ROI and take the top 20 items
                        combinedlist = combinedlist.sortedByDescending { it.roi }
                        combinedlist = combinedlist.take(20)
                        val combinedlist2 = PriceList(combinedlist)

                        //then we display the list in a recycler view
                        recyclerView = findViewById(R.id.recyclerView)
                        itemPriceDifferenceAdapter = ItemPriceDifferenceAdapter(combinedlist2)

                        recyclerView.adapter = itemPriceDifferenceAdapter
                        recyclerView.layoutManager = LinearLayoutManager(this)


                        val searchInput = findViewById<EditText>(R.id.searchInput)
                        val recyclerView2 = findViewById<RecyclerView>(R.id.recyclerView2)


                        recyclerView2.layoutManager = LinearLayoutManager(this)


                        searchInput.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                // Nothing to do here
                            }

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                // When the text changes, filter your data based on the search input
                                val searchQuery = s.toString().lowercase(Locale.ROOT)
                                val filteredList = searchlist.filter { item ->
                                    item.name?.lowercase(
                                        Locale.ROOT)
                                        ?.contains(searchQuery) ?: true
                                }
                                val filteredPriceList = PriceList(filteredList)

                                itemPriceDifferenceAdapter2 = ItemPriceDifferenceAdapter(filteredPriceList)
                                recyclerView2.adapter = itemPriceDifferenceAdapter2



                            }

                            override fun afterTextChanged(s: Editable?) {
                                // Nothing to do here

                            }
                        })

                    }

                }
            }
        }
    }

}




