package com.thuypham.ptithcm.editvideo.ui.mergeaudio

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.os.bundleOf
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentMergeAudioBinding
import com.thuypham.ptithcm.editvideo.extension.*
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.ui.fragment.media.MediaFragment
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.MergeAudioViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MergeAudioFragment : BaseFragment<FragmentMergeAudioBinding>(R.layout.fragment_merge_audio) {

    companion object {
        const val MAX_AUDIO_SELECT_COUNT = 20
    }

    private val mergeViewModel: MergeAudioViewModel by viewModel()
    private val mediaViewModel: MediaViewModel by sharedViewModel()
    private var mMediaPlayer: MediaPlayer? = null

    private var currentMedia: MediaFile? = null
    private var shouldNavigateToAudio = false

    private var audioSelected: ArrayList<MediaFile>? = null

    private val audioAdapter: AudioAdapter by lazy {
        AudioAdapter(onAudioPlayingUpdate = {
            currentMedia = it
            setupAudio(it)
        })
    }

    override fun setupView() {
        setupToolbar()
        setupEvent()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvAudio.adapter = audioAdapter
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        mediaViewModel.mediaSelected.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.btnMerge.gone()
                binding.tvEmptyAudioSelected.show()
            } else {
                audioSelected = it
                binding.btnMerge.show()
                binding.tvEmptyAudioSelected.gone()
                audioAdapter.submitList(it)
            }
        }

        mergeViewModel.mergeAudioResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading()
                    val data = response.data
                    if (shouldNavigateToAudio) {
                        shouldNavigateToAudio = false
                        navigateTo(
                            R.id.action_mergeAudio_to_audio_result,
                            bundleOf(HomeFragment.RESULT_PATH to data)
                        )
                    }
                }
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Failure -> {
                    shouldNavigateToAudio = false
                    response.extra?.let { showSnackBar(it) }
                    hideLoading()
                }
                else -> {
                    shouldNavigateToAudio = false
                    hideLoading()
                }
            }
        }

    }

    private fun setupEvent() {
        binding.apply {
            btnUploadAudio.setOnSingleClickListener {
                navigateTo(
                    R.id.action_mergeAudio_to_media, bundleOf(
                        MediaFragment.MEDIA_TYPE to MediaFile.MEDIA_TYPE_AUDIO,
                        MediaFragment.MAX_SELECTED_COUNT to MAX_AUDIO_SELECT_COUNT
                    )
                )
            }

            btnMerge.setOnSingleClickListener {
                mergeViewModel.mergeAudio(audioSelected ?: return@setOnSingleClickListener)
                shouldNavigateToAudio = true
            }
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(R.string.menu_merge_audios)
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
    }

    override fun onDetach() {
        super.onDetach()
        mediaViewModel.clearDataSelected()
    }

    override fun onPause() {
        super.onPause()
        releaseMedia()
    }

    private fun releaseMedia() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    private fun setupAudio(audioFile: MediaFile?) {
        if (audioFile == null) return
        releaseMedia()
        if (audioFile.isPlaying == true) {
            audioFile.path?.let { audioPath ->
                requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
                mMediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(audioPath))
                mMediaPlayer?.let {
                    it.start()
                    it.isLooping = true
                }
            }
        } else {
            releaseMedia()
        }
    }
}