package com.thuypham.ptithcm.editvideo.ui.mergeaudio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.databinding.ItemAudioBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.toTime
import com.thuypham.ptithcm.editvideo.model.MediaFile

class AudioAdapter(
    private val onItemSelected: ((item: MediaFile) -> Unit)? = null,
    private val onAudioPlayingUpdate: ((item: MediaFile) -> Unit)? = null,
) : ListAdapter<MediaFile, RecyclerView.ViewHolder>(DiffCallback()) {

    class AudioItemViewHolder(
        private val binding: ItemAudioBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaFile) {
            binding.apply {
                tvAudioPath.text = item.path
                setPlayBtnStatus(item.isPlaying ?: false)
                tvDuration.text = item.duration?.toTime()
            }
        }

        fun setPlayBtnStatus(isPlaying: Boolean) {
            if (isPlaying) {
                binding.ivPlay.setImageResource(R.drawable.ic_pause)
            } else {
                binding.ivPlay.setImageResource(R.drawable.ic_play)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AudioItemViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AudioItemViewHolder(binding)
            .apply {
                binding.root.setOnSingleClickListener {

                }
                binding.ivPlay.setOnSingleClickListener {
                    val item = currentList[absoluteAdapterPosition]
                    val isPlaying = !(item.isPlaying ?: false)
                    item.isPlaying = isPlaying
                    setPlayBtnStatus(isPlaying)
                    updatePlayingStatus(item, isPlaying)
                    onAudioPlayingUpdate?.invoke(item)
                }
            }
    }

    private fun updatePlayingStatus(item: MediaFile, isPlaying: Boolean) {
        currentList.forEach { mediaFile -> mediaFile.isPlaying = false }
        item.isPlaying = isPlaying
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AudioItemViewHolder).bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile) =
            oldItem == newItem
    }
}