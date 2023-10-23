package com.example.osrs_app.itemMods

import android.content.Context
import android.widget.TextView
import com.example.osrs_app.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class CustomMarkerView(context: Context, layoutResource: Int, private val chartType: String) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val formattedValue = String.format("%.0f", e?.y ?: 0f)
        tvContent.text = "$chartType: $formattedValue"
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        val offsetX = -(width / 2) - 100
        return MPPointF(offsetX.toFloat(), -height.toFloat())
    }
}
