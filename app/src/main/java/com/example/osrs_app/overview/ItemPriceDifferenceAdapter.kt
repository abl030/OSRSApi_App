package com.example.osrs_app.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.osrs_app.R

class ItemPriceDifferenceAdapter(private val itemList: List<ItemPriceDifference>) :
    RecyclerView.Adapter<ItemPriceDifferenceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_display, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textName)
        private val priceDifferenceTextView: TextView = itemView.findViewById(R.id.textPriceDifference)
        private val roiTextView: TextView = itemView.findViewById(R.id.textROI)

        fun bind(item: ItemPriceDifference) {
            nameTextView.text = "Name: ${item.name}"
            priceDifferenceTextView.text = "Price Difference: ${item.priceDifference}"
            roiTextView.text = "ROI: ${item.ROI}"
        }
    }
}
