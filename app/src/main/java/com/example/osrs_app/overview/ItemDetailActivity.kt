package com.example.osrs_app.overview

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.osrs_app.R
import com.example.osrs_app.itemMods.ChartType
import com.example.osrs_app.itemMods.CustomMarkerView
import com.example.osrs_app.itemMods.K
import com.example.osrs_app.itemMods.timeSeriesStats
import com.example.osrs_app.itemMods.updateChart
import com.github.mikephil.charting.charts.LineChart

class ItemDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        // Retrieve the data from the Intent
        val itemName = intent.getStringExtra("ITEM_NAME")
        val priceDifference = intent.getIntExtra("PRICE_DIFFERENCE", 0)
        val roi = intent.getIntExtra("ROI", 0)
        val iconUrl = intent.getStringExtra("ICON_URL")
        val itemId = intent.getStringExtra("ITEM_ID")

        //actually grab some data, remember to do this every view.
        val viewModel2: OverviewViewModel by viewModels()
        if (itemId != null) {
            viewModel2.fetchTimeSeriesData(itemId.toInt(), "5m")
        }

        // Get the chart view and timestats set up
        val lineChart: LineChart = findViewById(R.id.lineChart)
        val timeStats = findViewById<TextView>(R.id.timestats)

        // Update the chart when the activity is opened
        viewModel2.timeSeriesData.observe(this) { timeSeriesData ->
            updateChart(ChartType.HIGH_PRICE, timeSeriesData, lineChart)
            val mv = CustomMarkerView(this, chartType = "High Price" , layoutResource = R.layout.marker_view)
            lineChart.marker = mv
            //update the timestats entry for the dataset
            val (firstString, secondString) = timeSeriesStats(viewModel2.timeSeriesData.value)
            timeStats.text = "$firstString $secondString"
            }



        val tvItemName = findViewById<TextView>(R.id.tvItemName)
        val tvPriceDifference = findViewById<TextView>(R.id.tvPriceDifference)
        val tvROI = findViewById<TextView>(R.id.tvROI)
        val ivIcon = findViewById<ImageView>(R.id.ivIcon)
        val priceData = findViewById<TextView>(R.id.priceData)


        // Set the data to the respective views
        tvItemName.text = "$itemName"
        tvPriceDifference.text = "Price Difference: " + K(priceDifference)
        tvROI.text = "ROI: $roi%"
        priceData.text = ""

        // load the image from the API using glide.
        if (iconUrl != null) {
            val formattedIconUrl = iconUrl.replace(" ", "_")
            Glide.with(this)
                .load("https://oldschool.runescape.wiki/images/${formattedIconUrl}?format=png")
                .into(ivIcon)
        }

    // Set up the buttons to change the chart type
        val highPriceButton: Button = findViewById(R.id.highPriceButton)
        highPriceButton.setOnClickListener {
            updateChart(ChartType.HIGH_PRICE, viewModel2.timeSeriesData.value, lineChart)
            //create the markerview for the chart
            //to-do figure out a way to do this inside the update chart function
            val mv = CustomMarkerView(this, chartType = "High Price" , layoutResource = R.layout.marker_view)
            lineChart.marker = mv
                    }

        val lowPriceButton: Button = findViewById(R.id.lowPriceButton)
        lowPriceButton.setOnClickListener {
            updateChart(ChartType.LOW_PRICE, viewModel2.timeSeriesData.value, lineChart)
            //create the markerview for the chart
            //to-do figure out a way to do this inside the update chart function
            val mv = CustomMarkerView(this, chartType = "Low Price" , layoutResource = R.layout.marker_view)
            lineChart.marker = mv
        }

        val priceDeltaButton: Button = findViewById(R.id.priceDeltaButton)
        priceDeltaButton.setOnClickListener {
            updateChart(ChartType.PRICE_DELTA, viewModel2.timeSeriesData.value, lineChart)
            //create the markerview for the chart
            //to-do figure out a way to do this inside the update chart function
            val mv = CustomMarkerView(this, chartType = "Price Delta" , layoutResource = R.layout.marker_view)
            lineChart.marker = mv
        }



    }



}
