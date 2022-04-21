package com.thuypham.ptithcm.editvideo.ui.fragment.trimaudio

import android.annotation.SuppressLint
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.core.os.bundleOf
import com.google.android.material.slider.RangeSlider
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentTrimAudioBinding
import com.thuypham.ptithcm.editvideo.extension.*
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.ui.fragment.media.MediaFragment
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.TrimAudioViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class TrimAudioFragment : BaseFragment<FragmentTrimAudioBinding>(R.layout.fragment_trim_audio) {

    private val trimAudioViewModel: TrimAudioViewModel by viewModel()
    private val mediaViewModel: MediaViewModel by sharedViewModel()

    private var shouldNavigateToAudioResult = false
    private var currentAudio: MediaFile? = null
    private var mMediaPlayer: MediaPlayer? = null

    private var startTime = 0
    private var endTime = 0

    private lateinit var runnable: Runnable
    private lateinit var handler: Handler

    override fun setupView() {
        setupToolbar()
        setupEvent()
        setupAudio()
    }

    private fun setupEvent() {
        binding.apply {

            rangeSlider.setLabelFormatter { value ->
                val time = value.toLong().toTimeAsHHmmSSS()
                time
            }

            rangeSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                @SuppressLint("RestrictedApi")
                override fun onStartTrackingTouch(slider: RangeSlider) {
                }

                @SuppressLint("RestrictedApi")
                override fun onStopTrackingTouch(slider: RangeSlider) {
                    startTime = rangeSlider.values[0].toInt()
                    endTime = rangeSlider.values[1].toInt()
                    Log.d("Thuys", "$startTime:$startTime-${startTime.toSecond()}")
                    tvDurationStart.text = startTime.toLong().toTimeAsHHmmSSS()
                    tvDurationEnd.text = endTime.toLong().toTimeAsHHmmSSS()
                    mMediaPlayer?.seekTo(startTime)
                }

            })

            btnTrimAudio.setOnSingleClickListener {
                if (currentAudio == null || currentAudio?.path == null) {
                    showSnackBar(R.string.empty_audio_error)
                } else {
                    shouldNavigateToAudioResult = true
                    trimAudioViewModel.trimAudio(currentAudio?.path!!, startTime, endTime)
                }
            }
            btnUploadAudio.setOnSingleClickListener {
                navigateTo(
                    R.id.media,
                    bundleOf(
                        MediaFragment.MEDIA_TYPE to MediaFile.MEDIA_TYPE_AUDIO,
                        MediaFragment.MAX_SELECTED_COUNT to 1
                    )
                )
            }
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(R.string.menu_trim_audio)
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        trimAudioViewModel.trimAudioResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading()
                    val data = response.data
                    if (shouldNavigateToAudioResult) {
                        shouldNavigateToAudioResult = false
                        navigateTo(
                            R.id.trim_to_audio_result,
                            bundleOf(HomeFragment.RESULT_PATH to data)
                        )
                    }
                }
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Failure -> {
                    shouldNavigateToAudioResult = false
                    response.extra?.let { showSnackBar(it) }
                    hideLoading()
                }
                else -> {
                    shouldNavigateToAudioResult = false
                    hideLoading()
                }
            }
        }

        mediaViewModel.audioMedia.observe(viewLifecycleOwner) {
            currentAudio = it
            setupAudio()
        }

        runnable = Runnable {
            if (mMediaPlayer?.currentPosition ?: 0 >= endTime) {
                mMediaPlayer?.seekTo(startTime)
            }
            runnable.let { handler.postDelayed(it, 500) }
        }
        handler = Handler()
        handler.postDelayed(runnable, 500)
    }

    private fun setupAudio() {
        if (currentAudio != null) {
            releaseMedia()
            binding.groupAudio.show()
            val audioPath = currentAudio!!.path
            binding.tvAudioPath.text = audioPath

            requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
            mMediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(audioPath))

            mMediaPlayer?.let {
                it.start()
                it.isLooping = true
            }

            if (mMediaPlayer?.duration ?: 0 > 0) {
                try {
                    endTime = mMediaPlayer?.duration ?: 0
                    startTime = 0
                    binding.apply {
                        rangeSlider.show()
                        rangeSlider.valueFrom = startTime.toFloat()
                        rangeSlider.valueTo = endTime.toFloat()
                        rangeSlider.values = arrayListOf(startTime.toFloat(), endTime.toFloat())
                        tvDurationStart.text = rangeSlider.values[0].toLong().toTimeAsHHmmSSS()
                        tvDurationEnd.text = rangeSlider.values[1].toLong().toTimeAsHHmmSSS()
                        mMediaPlayer?.seekTo(startTime)
                    }
                } catch (ex: Exception) {
                    Log.e("Thuy", ex.printStackTrace().toString())
                }
            } else {
                binding.rangeSlider.hide()
            }
        } else {
            binding.groupAudio.hide()
            binding.rangeSlider.hide()
            releaseMedia()
        }
    }

    override fun onStop() {
        super.onStop()
        mMediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        mMediaPlayer?.start()
    }

    private fun releaseMedia() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }
}