package com.example.osrs_app.overview


import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.osrs_app.R
import com.example.osrs_app.itemMods.kFormatter

class ItemPriceDifferenceAdapter(private val itemList: List<ItemPriceDifference>) :
    RecyclerView.Adapter<ItemPriceDifferenceAdapter.ViewHolder>() {
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_display, parent, false)
        return ViewHolder(view)
    }
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return itemList.size
    }
    // Provide a reference to the views for each data item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textName)
        private val priceDifferenceTextView: TextView =
            itemView.findViewById(R.id.textPriceDifference)
        private val roiTextView: TextView = itemView.findViewById(R.id.textROI)
        private val iconImageView: ImageView = itemView.findViewById(R.id.itemIcon)


        // Define the bind function to bind data to the ViewHolder
        @SuppressLint("SetTextI18n")
        fun bind(item: ItemPriceDifference) {
            nameTextView.text = "${item.name}"
            priceDifferenceTextView.text = "Price Difference: ${kFormatter(item.priceDifference)}"
            roiTextView.text = "ROI: ${String.format("%.2f", item.roi)}%"


            // Replace spaces with underscores in the iconUrl
            val icon = item.icon?.replace(" ", "_")

            // Load and display the image using Glide
            item.icon?.let {
                Glide.with(itemView.context)
                    .load("https://oldschool.runescape.wiki/images/${icon}?format=png")
                    .into(iconImageView)
            }
            //set the on click listener for the item view
            //assign all the variables to pass through as intent on item click.
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ItemDetailActivity::class.java)
                intent.putExtra("ITEM_NAME", item.name)
                intent.putExtra("PRICE_DIFFERENCE", item.priceDifference)
                intent.putExtra("ROI", item.roi)
                intent.putExtra("ICON_URL", item.icon)
                intent.putExtra("ITEM_ID", item.itemId)
                intent.putExtra("HIGH_PRICE", item.highPrice)
                intent.putExtra("LOW_PRICE", item.lowPrice)
                intent.putExtra("LIMIT", item.limit)
                intent.putExtra("EXAMINE", item.examine)
                itemView.context.startActivity(intent)

        }

        }
    }


}