package com.duongvv.photoeditor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.duongvv.photoeditor.databinding.ItemDateHeaderBinding
import com.duongvv.photoeditor.databinding.ItemImageBinding

class ImageAdapter(
    private val items: List<Any>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_TYPE_IMAGE = 0
    private val ITEM_TYPE_MONTH_HEADER = 1

    inner class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)
    inner class MonthHeaderViewHolder(val binding: ItemDateHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String && (items[position] as String).contains(" ")) {
            ITEM_TYPE_MONTH_HEADER
        } else {
            ITEM_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_IMAGE) {
            val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            // Set image height == 1/5 of the screen height
            val displayMetrics = parent.context.resources.displayMetrics
            val height = displayMetrics.heightPixels / 5
            binding.root.layoutParams.height = height

            ImageViewHolder(binding)
        } else {
            val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MonthHeaderViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            val imagePath = items[position] as String

            // Use Glide to display image
            Glide.with(holder.binding.imageView.context)
                .load(imagePath)
                .into(holder.binding.imageView)

            holder.binding.imageView.setOnClickListener {
                onImageClick(imagePath)  // Callback with image path
            }
        } else if (holder is MonthHeaderViewHolder) {
            val monthHeader = items[position] as String
            holder.binding.dateTextView.text = monthHeader
        }
    }

    override fun getItemCount(): Int = items.size
}
