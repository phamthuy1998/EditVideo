package com.thuypham.ptithcm.editvideo.ui.fragment.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.databinding.ItemMediaBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.showSnackBar
import com.thuypham.ptithcm.editvideo.extension.toTime
import com.thuypham.ptithcm.editvideo.model.MediaFile
import java.io.File

class MediaAdapter(
    private val maxSelectCount: Int,
    private val onItemSelected: ((item: MediaFile) -> Unit)? = null,
) : ListAdapter<MediaFile, RecyclerView.ViewHolder>(DiffCallback()) {

    private var listItemSelected: ArrayList<MediaFile> = arrayListOf<MediaFile>()

    private var canSelected: Boolean = true

    fun setCanSelected(canSelect: Boolean) {
        canSelected = canSelect
    }

    class MediaItemViewHolder(
        private val binding: ItemMediaBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaFile) {
            binding.apply {
                if (item.isAudio) {
                    ivMedia.setImageResource(R.drawable.ic_audio)
                } else {
                    item.path?.let {
                        Glide.with(root.context)
                            .load(File(it))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(ivMedia)
                    }
                }

                tvDuration.isVisible = item.isVideo || item.isAudio
                tvDuration.text = item.duration?.toTime()
                viewSelected.isVisible = item.isSelected ?: false

                tvName.isVisible = item.isAudio
                tvName.text = item.displayName
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MediaItemViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaItemViewHolder(binding)
            .apply {
                binding.root.setOnSingleClickListener {
                    if (listItemSelected.size < maxSelectCount) {
                        val mediaFile = currentList[absoluteAdapterPosition]
                        val isSelected = !(mediaFile.isSelected ?: false)
                        mediaFile.isSelected = isSelected
                        binding.viewSelected.isVisible = isSelected

                        if (isSelected) {
                            listItemSelected.add(mediaFile)
                        } else {
                            listItemSelected.remove(mediaFile)
                        }

                        onItemSelected?.invoke(mediaFile)
                    } else {
                        parent.showSnackBar(
                            parent.context.getString(
                                R.string.max_selected_count_error,
                                maxSelectCount
                            )
                        )
                    }
                }
            }
    }

    fun getListMediaSelected(): ArrayList<MediaFile> {
        return listItemSelected
    }

    fun selectedCount(): Int {
        return listItemSelected.size
    }

    fun setListSelected(list: ArrayList<MediaFile>?) {
        if (!list.isNullOrEmpty()) {
            listItemSelected = list
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MediaItemViewHolder).bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile) =
            oldItem == newItem
    }
}