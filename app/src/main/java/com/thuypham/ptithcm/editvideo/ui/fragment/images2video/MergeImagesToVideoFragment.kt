package com.thuypham.ptithcm.editvideo.ui.fragment.images2video

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.os.bundleOf
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentMergeImagesBinding
import com.thuypham.ptithcm.editvideo.extension.goBack
import com.thuypham.ptithcm.editvideo.extension.navigateTo
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.shakeAnim
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.dialog.ConfirmDialog
import com.thuypham.ptithcm.editvideo.ui.fragment.extractimage.ImageAdapter
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.ui.fragment.imagedetail.ImageDetailDialogFragment
import com.thuypham.ptithcm.editvideo.ui.fragment.media.MediaFragment
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.MergeImageViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MergeImagesToVideoFragment :
    BaseFragment<FragmentMergeImagesBinding>(R.layout.fragment_merge_images) {

    private val mergeImageViewModel: MergeImageViewModel by viewModel()
    private val mediaViewModel: MediaViewModel by sharedViewModel()

    private var listImagesSelected: ArrayList<MediaFile>? = null
    private var audioPath: String? = null
    private var shouldNavigateToResultFragment = false
    private var mMediaPlayer: MediaPlayer? = null

    private val imageAdapter: ImageAdapter by lazy {
        ImageAdapter {
            onImageItemAdapterClick(it)
        }
    }

    companion object {
        const val MAX_IMAGE = 30
        const val MAX_SECOND_PER_IMAGE = 20
    }

    private fun onImageItemAdapterClick(imagePath: String) {
        ImageDetailDialogFragment(imagePath).show(parentFragmentManager, ConfirmDialog.TAG)
    }

    override fun setupView() {
        setupToolbar()
        setupEvent()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.menu_merge_image))
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
    }

    private fun setupRecyclerView() {
        binding.rvImagesSelected.adapter = imageAdapter
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        mediaViewModel.mediaSelected.observe(viewLifecycleOwner) {
            listImagesSelected = it
            imageAdapter.submitList(getListAudioPath())
        }
        mediaViewModel.audioMedia.observe(viewLifecycleOwner) {
            audioPath = it?.path
            audioPath?.let { audioPath ->
                binding.tvAudioPath.text = audioPath
                setupAudio(audioPath)
            }
        }
        mergeImageViewModel.mergeImageResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading()
                    if (shouldNavigateToResultFragment) {
                        shouldNavigateToResultFragment = false
                        navigateTo(
                            R.id.mergeImages_to_result,
                            bundleOf(HomeFragment.RESULT_PATH to response.data)
                        )
                    }
                }
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Failure -> {
                    shouldNavigateToResultFragment = false
                    hideLoading()
                    response.extra?.let { showSnackBar(it) }
                }
                else -> {
                    shouldNavigateToResultFragment = false
                    hideLoading()
                }
            }
        }
    }

    private fun getListAudioPath(): List<String?>? {
        return listImagesSelected?.map { it.path }
    }

    private fun setupEvent() {
        binding.apply {
            btnMerge.setOnSingleClickListener {
                mergeImageToVideo()
            }
            btnUploadAudio.setOnSingleClickListener {
                navigateTo(
                    R.id.media, bundleOf(
                        MediaFragment.MEDIA_TYPE to MediaFile.MEDIA_TYPE_AUDIO,
                        MediaFragment.MAX_SELECTED_COUNT to 1
                    )
                )
            }
            btnUploadImage.setOnSingleClickListener {
                navigateTo(
                    R.id.media, bundleOf(
                        MediaFragment.MEDIA_TYPE to MediaFile.MEDIA_TYPE_IMAGE,
                        MediaFragment.MAX_SELECTED_COUNT to MAX_IMAGE
                    )
                )
            }
        }
    }

    private fun mergeImageToVideo() {
        val secondPerImgStr = binding.edtSecond.text.toString()
        if (secondPerImgStr.isBlank()) {
            showSnackBar(R.string.empty_second_error)
            binding.edtSecond.error = getString(R.string.empty_second_error)
            binding.edtSecond.shakeAnim()
            return
        }
        val secondPerImage = secondPerImgStr.toInt()
        if (secondPerImage <= 0 || secondPerImage > MAX_SECOND_PER_IMAGE) {
            showSnackBar(R.string.second_per_image_des)
            binding.edtSecond.shakeAnim()
            return
        }

        if (listImagesSelected.isNullOrEmpty()) {
            binding.btnUploadImage.shakeAnim()
            showSnackBar(R.string.select_image_empty)
            return
        }
        shouldNavigateToResultFragment = true
        mergeImageViewModel.mergeImageToVideo(
            listImagesSelected!!,
            secondPerImgStr.toInt(),
            audioPath
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaViewModel.clearDataSelected()
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }

    }

    private fun setupAudio(audioPath: String) {
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        mMediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(audioPath))

        mMediaPlayer?.let {
            it.setOnCompletionListener {

            }
            it.start()
            it.isLooping = true
        }
    }

}