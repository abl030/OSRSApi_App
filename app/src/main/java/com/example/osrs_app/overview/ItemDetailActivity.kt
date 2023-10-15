package com.example.osrs_app.overview

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.osrs_app.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

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

        val lineChart = findViewById<LineChart>(R.id.lineChart)

        viewModel2.timeSeriesData.observe(this) { timeSeriesData ->
            if (timeSeriesData != null) {

                // Add the data to the chart


                val entries = ArrayList<Entry>()

                timeSeriesData.data.forEach {
                    // Using timestamp for X and avgHighPrice for Y axis
                    entries.add(Entry(it.timestamp.toFloat(), it.avgHighPrice?.toFloat() ?: 0f))
                }

                val lineDataSet = LineDataSet(entries, "High Price over Time")
                val lineData = LineData(lineDataSet)
                lineChart.data = lineData

                // Optional: Customize the appearance of the chart
                //lineDataSet.color = Color.BLUE
                //lineDataSet.valueTextColor = Color.BLACK
                lineDataSet.valueTextSize = 12f

                // Refresh the chart
                lineChart.invalidate()
            }


        }
    }
}