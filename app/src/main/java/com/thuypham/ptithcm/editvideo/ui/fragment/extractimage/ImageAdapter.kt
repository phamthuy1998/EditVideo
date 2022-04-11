package com.thuypham.ptithcm.editvideo.ui.fragment.extractimage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.databinding.ItemMediaBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import java.io.File

class ImageAdapter(
    private val onItemSelected: ((item: String) -> Unit)? = null,
) : ListAdapter<String, RecyclerView.ViewHolder>(DiffCallback()) {

    class ImageViewHolderItem(
        private val binding: ItemMediaBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.apply {
                Glide.with(root.context)
                    .load(File(item))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(ivMedia)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImageViewHolderItem {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolderItem(binding)
            .apply {
                binding.root.setOnSingleClickListener {
                    val filePath = currentList[absoluteAdapterPosition]
                    onItemSelected?.invoke(filePath)
                }
            }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ImageViewHolderItem).bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String) =
            oldItem == newItem
    }
}