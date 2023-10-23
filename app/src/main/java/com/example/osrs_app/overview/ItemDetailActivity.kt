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
import com.example.osrs_app.itemMods.averageHighTimeStepInMinutes
import com.example.osrs_app.itemMods.averageLowTimeStepInMinutes
import com.example.osrs_app.itemMods.formatMinutes
import com.example.osrs_app.itemMods.formatRatio
import com.example.osrs_app.itemMods.hightolowratioString
import com.example.osrs_app.itemMods.kFormatter
import com.example.osrs_app.itemMods.limitFormatter
import com.example.osrs_app.itemMods.potentialProfitPerHour
import com.example.osrs_app.itemMods.profitPerFlipInt
import com.example.osrs_app.itemMods.ratioWarnings
import com.example.osrs_app.itemMods.suggestedBuyOfferPriceInt
import com.example.osrs_app.itemMods.suggestedProfitPerHourInt
import com.example.osrs_app.itemMods.suggestedSellOfferPriceInt
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
        val highPrice = intent.getIntExtra("HIGH_PRICE", 0)
        val lowPriceIntent = intent.getIntExtra("LOW_PRICE", 0)
        val limit = intent.getIntExtra("LIMIT", 0)
        val examine = intent.getStringExtra("EXAMINE")

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
            }



        val tvItemName = findViewById<TextView>(R.id.tvItemName)
        val tvPriceDifference = findViewById<TextView>(R.id.tvPriceDifference)
        val tvROI = findViewById<TextView>(R.id.tvROI)
        val ivIcon = findViewById<ImageView>(R.id.ivIcon)
        val highPriceIntent = findViewById<TextView>(R.id.HighPrice)
        val lowPrice = findViewById<TextView>(R.id.LowPrice)
        val limitView = findViewById<TextView>(R.id.Limit)
        val examineView = findViewById<TextView>(R.id.Examine)
        val averageBuyTime = findViewById<TextView>(R.id.AverageBuyTime)
        val averageSellTime = findViewById<TextView>(R.id.AverageSellTime)
        val timeSeriesRatio = findViewById<TextView>(R.id.Ratio)
        val timeSeriesRatioWarning = findViewById<TextView>(R.id.RatioWarning)
        val profitFlip = findViewById<TextView>(R.id.profitPerFlip)
        val potProfitHour = findViewById<TextView>(R.id.potProfitHour)
        val suggestedBuyPrice = findViewById<TextView>(R.id.suggestedBuyPrice)
        val suggestedSellPrice = findViewById<TextView>(R.id.suggestedSellPrice)




        // Set the data to the respective views
        tvItemName.text = "$itemName"
        tvPriceDifference.text = "Price Difference: " + kFormatter(priceDifference)
        tvROI.text = "ROI: $roi%"
        highPriceIntent.text = "High Price: " + kFormatter(highPrice)
        lowPrice.text = "Low Price: " + kFormatter(lowPriceIntent)
        limitView.text = "Limit: " + limitFormatter( limit )
        examineView.text = examine
        viewModel2.timeSeriesData.observe(this) {
            averageSellTime.text =
                "Avg Sell Time: " + formatMinutes(averageHighTimeStepInMinutes(viewModel2.timeSeriesData.value))

            averageBuyTime.text =
                "Avg Buy Time: " + formatMinutes(averageLowTimeStepInMinutes(viewModel2.timeSeriesData.value))

            timeSeriesRatio.text = formatRatio(hightolowratioString(viewModel2.timeSeriesData.value))

            timeSeriesRatioWarning.text = ratioWarnings(hightolowratioString(viewModel2.timeSeriesData.value))

            profitFlip.text = profitPerFlipInt(viewModel2.timeSeriesData.value)?.let { it1 ->
                kFormatter(
                    it1)
            }

            potProfitHour.text = potentialProfitPerHour(viewModel2.timeSeriesData.value)?.let { it1 ->
                kFormatter(
                    it1)
            }

            suggestedBuyPrice.text = suggestedBuyOfferPriceInt(viewModel2.timeSeriesData.value)?.let { it1 ->
                kFormatter(
                    it1)
            }

            suggestedSellPrice.text = suggestedSellOfferPriceInt(viewModel2.timeSeriesData.value)?.let { it1 ->
                kFormatter(
                    it1)
            }

            timeStats.text = "Potential profit per hour with suggested strategy is "+ kFormatter(suggestedProfitPerHourInt(viewModel2.timeSeriesData.value)) + " gp/h"



        }
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
