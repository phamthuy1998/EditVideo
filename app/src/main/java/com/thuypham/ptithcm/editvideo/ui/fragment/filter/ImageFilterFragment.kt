package com.thuypham.ptithcm.editvideo.ui.fragment.filter

import android.util.Log
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentImageFilterBinding
import com.thuypham.ptithcm.editvideo.extension.navigateTo
import com.thuypham.ptithcm.editvideo.extension.show
import com.thuypham.ptithcm.editvideo.model.ImageFilter
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.ui.fragment.media.MediaFragment
import com.thuypham.ptithcm.editvideo.viewmodel.ImageFilterViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.imageResource
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class ImageFilterFragment (): BaseFragment<FragmentImageFilterBinding>(R.layout.fragment_image_filter) {

    private val mediaViewModel: MediaViewModel by sharedViewModel()
    private val imageFilterViewModel: ImageFilterViewModel by viewModel()
    private val filterAdapter: ImageFilterAdapter by lazy { ImageFilterAdapter(::onItemSelected) }

    private fun onItemSelected(imageFilter: ImageFilter) {
        binding.ivFilter.imageBitmap = imageFilter.bitmap
    }

    override fun setupView() {
        setupToolbar()
        setupFilterAdapter()
    }

    private fun setupFilterAdapter() {
        binding.run {
            rvFilter.adapter = filterAdapter
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.add_filter_for_image))
        setLeftBtn(R.drawable.ic_add_video) {
            navigateTo(
                R.id.media, bundleOf(
                    MediaFragment.MEDIA_TYPE to MediaFile.MEDIA_TYPE_IMAGE,
                    MediaFragment.MAX_SELECTED_COUNT to 1
                )
            )
        }
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        mediaViewModel.mediaSelected.observe(viewLifecycleOwner) { imageFiles ->
            loadImage(imageFilterViewModel.imageMedia)
            imageFilterViewModel.getImageFilterList(imageFiles?.firstOrNull(), context)
            Log.d(TAG, "setupDataObserver: $imageFiles")
        }

        imageFilterViewModel.imageFilterItemsLiveData.observe(viewLifecycleOwner) { imageFilter ->
            Log.d(TAG, "setupDataObserver: imageFilter: $imageFilter")
            binding.rvFilter.show()
            filterAdapter.submitList(imageFilter)
        }
    }

    private fun loadImage(mediaFile: MediaFile?) {
        binding.apply {
            Glide.with(this@ImageFilterFragment)
                .load(File(mediaFile?.path ?: ""))
                .placeholder(R.drawable.ic_image_placeholder)
                .into(ivFilter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaViewModel.clearDataSelected()
    }
}