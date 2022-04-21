package com.thuypham.ptithcm.editvideo.ui.fragment.mergevideo

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentMergeVideoBinding
import com.thuypham.ptithcm.editvideo.extension.*
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.dialog.ConfirmDialog
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.ui.fragment.imagedetail.ImageDetailDialogFragment
import com.thuypham.ptithcm.editvideo.ui.fragment.media.MediaFragment
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.MergeVideoViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MergeVideoFragment : BaseFragment<FragmentMergeVideoBinding>(R.layout.fragment_merge_video) {
    companion object {
        const val MAX_VIDEO_SELECT_COUNT = 20
    }

    private val mediaViewModel: MediaViewModel by sharedViewModel()
    private val mergeVideoViewModel: MergeVideoViewModel by viewModel()


    private var listVideosSelected: ArrayList<MediaFile>? = null
    private var audioPath: String? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var shouldNavigateToResultFragment = false

    private val imageVideoAdapter: ImageVideoAdapter by lazy {
        ImageVideoAdapter {
            it.path?.let { it1 -> onImageItemAdapterClick(it1) }
        }
    }

    private fun onImageItemAdapterClick(imagePath: String) {
        ImageDetailDialogFragment(imagePath).show(parentFragmentManager, ConfirmDialog.TAG)
    }

    override fun setupView() {
        setupToolbar()
        setupRecyclerView()
        setupEvent()
    }

    private fun setupEvent() {
        binding.apply {
            btnMerge.setOnSingleClickListener {
                if (listVideosSelected.isNullOrEmpty()) {
                    showSnackBar(R.string.please_upload_videos)
                } else {
                    shouldNavigateToResultFragment = true
                    mergeVideoViewModel.mergeVideo(
                        listVideosSelected!!,
                        checkRemoveAudio.isChecked, null,
                        audioPath
                    )
                }
            }

            ivRemoveAudio.setOnSingleClickListener {
                mediaViewModel.audioMedia.value = null
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvVideo.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            adapter = imageVideoAdapter
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(R.string.menu_merge_video)

        setRightBtn(R.drawable.ic_add_video) {
            navigateTo(
                R.id.action_mergeVideo_to_media, bundleOf(
                    MediaFragment.MEDIA_TYPE to MediaFile.MEDIA_TYPE_VIDEO,
                    MediaFragment.MAX_SELECTED_COUNT to MAX_VIDEO_SELECT_COUNT
                )
            )

        }
        setSubRightBtn(R.drawable.ic_add_audio) {
            navigateTo(
                R.id.action_mergeVideo_to_media, bundleOf(
                    MediaFragment.MEDIA_TYPE to MediaFile.MEDIA_TYPE_AUDIO,
                    MediaFragment.MAX_SELECTED_COUNT to 1
                )
            )
        }
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        mediaViewModel.mediaSelected.observe(viewLifecycleOwner) {
            listVideosSelected = it
            if (it.isNullOrEmpty()) {
                binding.checkRemoveAudio.gone()
                binding.tvEmptyList.show()
                binding.btnMerge.gone()
            } else {
                binding.btnMerge.show()
                binding.tvEmptyList.gone()
                binding.checkRemoveAudio.show()
                imageVideoAdapter.submitList(it)
            }
        }
        mediaViewModel.audioMedia.observe(viewLifecycleOwner) {
            audioPath = it?.path
            binding.groupAudioPath.isVisible = audioPath != null
            binding.tvAudioPath.text = audioPath
            setupAudio(audioPath)
        }

        mergeVideoViewModel.mergeVideoResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading()
                    val data = response.data
                    if (shouldNavigateToResultFragment) {
                        shouldNavigateToResultFragment = false
                        navigateTo(
                            R.id.merge_vid_to_result,
                            bundleOf(HomeFragment.RESULT_PATH to data)
                        )
                    }
                }
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Failure -> {
                    shouldNavigateToResultFragment = false
                    response.extra?.let { showSnackBar(it) }
                    hideLoading()
                }
                else -> {
                    shouldNavigateToResultFragment = false
                    hideLoading()
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mediaViewModel.clearDataSelected()
    }

    override fun onResume() {
        super.onResume()
        mMediaPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        mMediaPlayer?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseMedia()
    }

    private fun releaseMedia() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    private fun setupAudio(audioPath: String?) {
        if (audioPath == null) {
            releaseMedia()
            return
        }
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