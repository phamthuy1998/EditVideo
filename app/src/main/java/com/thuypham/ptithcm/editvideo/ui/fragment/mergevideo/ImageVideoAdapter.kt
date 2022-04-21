package com.thuypham.ptithcm.editvideo.ui.fragment.mergevideo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.databinding.ItemVideoImageBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.toTime
import com.thuypham.ptithcm.editvideo.model.MediaFile
import java.io.File

class ImageVideoAdapter(
    private val onItemSelected: ((item: MediaFile) -> Unit)? = null,
) : ListAdapter<MediaFile, RecyclerView.ViewHolder>(DiffCallback()) {

    class ImageVideoItemViewHolder(
        private val binding: ItemVideoImageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaFile) {
            binding.apply {
                tvDuration.text = item.duration?.toTime()
                Glide.with(root.context)
                    .load(File(item.path))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(ivImage)
                tvDuration.isVisible = item.isVideo
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImageVideoItemViewHolder {
        val binding =
            ItemVideoImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageVideoItemViewHolder(binding)
            .apply {
                binding.root.setOnSingleClickListener {
                    onItemSelected?.invoke(currentList[absoluteAdapterPosition])
                }

            }
    }

    private fun updatePlayingStatus(item: MediaFile, isPlaying: Boolean) {
        currentList.forEach { mediaFile -> mediaFile.isPlaying = false }
        item.isPlaying = isPlaying
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ImageVideoItemViewHolder).bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile) =
            oldItem == newItem
    }
}