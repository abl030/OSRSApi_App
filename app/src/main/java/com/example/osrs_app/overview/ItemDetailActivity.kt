package com.example.osrs_app.overview

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.osrs_app.R

class ItemDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        val tvItemName = findViewById<TextView>(R.id.tvItemName)
        val tvPriceDifference = findViewById<TextView>(R.id.tvPriceDifference)
        val tvROI = findViewById<TextView>(R.id.tvROI)
        val ivIcon = findViewById<ImageView>(R.id.ivIcon)

        // Retrieve the data from the Intent
        val itemName = intent.getStringExtra("ITEM_NAME")
        val priceDifference = intent.getIntExtra("PRICE_DIFFERENCE", 0)
        val roi = intent.getIntExtra("ROI", 0)
        val iconUrl = intent.getStringExtra("ICON_URL")

        // Set the data to the respective views
        tvItemName.text = "Item Name: $itemName"
        tvPriceDifference.text = "Price Difference: $priceDifference"
        tvROI.text = "ROI: $roi"

        // load the image from the API using glide.
        if (iconUrl != null) {
            val formattedIconUrl = iconUrl.replace(" ", "_")
            Glide.with(this)
                .load("https://oldschool.runescape.wiki/images/${formattedIconUrl}?format=png")
                .into(ivIcon)
        }
    }
}
