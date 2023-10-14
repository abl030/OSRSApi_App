package com.example.osrs_app.overview


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.osrs_app.R
import com.example.osrs_app.itemMods.K

class ItemPriceDifferenceAdapter(private val itemList: List<ItemPriceDifference>) :
    RecyclerView.Adapter<ItemPriceDifferenceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_display, parent, false)
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
        private val priceDifferenceTextView: TextView =
            itemView.findViewById(R.id.textPriceDifference)
        private val roiTextView: TextView = itemView.findViewById(R.id.textROI)
        private val iconImageView: ImageView = itemView.findViewById(R.id.itemIcon)

        fun bind(item: ItemPriceDifference) {
            nameTextView.text = "${item.name}"
            priceDifferenceTextView.text = "Price Difference: ${K(item.priceDifference)}K"
            roiTextView.text = "ROI: ${item.ROI}%"

            // Replace spaces with underscores in the iconUrl
            val icon = item.icon?.replace(" ", "_")

            // Load and display the image using Glide
            item.icon?.let {
                Glide.with(itemView.context)
                    .load("https://oldschool.runescape.wiki/images/${icon}?format=png")
                    .into(iconImageView)
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ItemDetailActivity::class.java)
                intent.putExtra("ITEM_NAME", item.name)
                intent.putExtra("PRICE_DIFFERENCE", item.priceDifference)
                intent.putExtra("ROI", item.ROI)
                intent.putExtra("ICON_URL", item.icon)
                itemView.context.startActivity(intent)

        }

        }
    }


}