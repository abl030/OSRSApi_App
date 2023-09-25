package com.example.osrs_app


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
        val viewModel = ViewModelProvider(this)[OverviewViewModel::class.java]
        val itemIdToFind = 9297 // Replace with the desired item ID

        viewModel.fetchLatestData()
        viewModel.fetchMappingInfo()

        // Observe latestData, mappingInfo, and error LiveData properties
        viewModel.latestData.observe(this) { latestData ->
            if (latestData != null) {
                val item = latestData.data[itemIdToFind.toString()]
                // Update the UI with item data
                textView.text = item.toString()


            } else {
                // Handle the case when latestData is null
                textView.text = "test2"
            }

        }
    }
}





