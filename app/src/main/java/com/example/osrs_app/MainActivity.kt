package com.example.osrs_app


import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.osrs_app.itemMods.calculateROI
import com.example.osrs_app.overview.CombinedItem
import com.example.osrs_app.overview.OverviewViewModel


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
        viewModel.combineLatestAndMappingData()


        // Observe changes in the cobmineddata LiveData
        viewModel.combinedList.observe(this) { combinedList ->
            // Update the UI
            textView.text = combinedList.toString()

        }
    }



}





