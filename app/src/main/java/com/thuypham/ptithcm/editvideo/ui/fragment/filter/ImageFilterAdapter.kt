package com.thuypham.ptithcm.editvideo.ui.fragment.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.databinding.LayoutItemFilterBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.model.ImageFilter

class ImageFilterAdapter(
    val onItemSelected: (ImageFilter) -> Unit
) : ListAdapter<ImageFilter, ImageFilterAdapter.ImageFilterViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<ImageFilter>() {
        override fun areItemsTheSame(oldItem: ImageFilter, newItem: ImageFilter) =
            oldItem.filterType == newItem.filterType

        override fun areContentsTheSame(oldItem: ImageFilter, newItem: ImageFilter) =
            oldItem.hashCode() == newItem.hashCode()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFilterViewHolder {
        val binding = LayoutItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageFilterViewHolder(binding).apply {
            binding.root.setOnSingleClickListener {
                onItemSelected.invoke(getItem(absoluteAdapterPosition))
            }
        }
    }

    override fun onBindViewHolder(holder: ImageFilterViewHolder, position: Int) {
        holder.bindView(getItem(position))
    }

    class ImageFilterViewHolder(private val binding: LayoutItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(imageFilter: ImageFilter) {
            binding.run {
                Glide.with(root).load(imageFilter.bitmap).placeholder(R.drawable.ic_image_placeholder).into(ivFilter)
                tvFilterName.text = imageFilter.filterName
            }
        }
    }

}