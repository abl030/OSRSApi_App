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
            viewModel2.fetchTimeSeriesData(itemId.toInt(), "24h")
        }


        val tvItemName = findViewById<TextView>(R.id.tvItemName)
        val tvPriceDifference = findViewById<TextView>(R.id.tvPriceDifference)
        val tvROI = findViewById<TextView>(R.id.tvROI)
        val ivIcon = findViewById<ImageView>(R.id.ivIcon)


        // Set the data to the respective views
        tvItemName.text = "Item Name: $itemName"
        tvPriceDifference.text = "Price Difference: $priceDifference"
        tvROI.text = "ROI: $roi%"

        // load the image from the API using glide.
        if (iconUrl != null) {
            val formattedIconUrl = iconUrl.replace(" ", "_")
            Glide.with(this)
                .load("https://oldschool.runescape.wiki/images/${formattedIconUrl}?format=png")
                .into(ivIcon)
        }

        val lineChart: LineChart = findViewById(R.id.lineChart)

        updateChart(ChartType.HIGH_PRICE, viewModel2.timeSeriesData.value, lineChart)

        val highPriceButton: Button = findViewById(R.id.highPriceButton)
        highPriceButton.setOnClickListener {
            updateChart(ChartType.HIGH_PRICE, viewModel2.timeSeriesData.value, lineChart)
        }

        val lowPriceButton: Button = findViewById(R.id.lowPriceButton)
        lowPriceButton.setOnClickListener {
            updateChart(ChartType.LOW_PRICE, viewModel2.timeSeriesData.value, lineChart)
        }

        val priceDeltaButton: Button = findViewById(R.id.priceDeltaButton)
        priceDeltaButton.setOnClickListener {
            updateChart(ChartType.PRICE_DELTA, viewModel2.timeSeriesData.value, lineChart)
        }
    }



}
