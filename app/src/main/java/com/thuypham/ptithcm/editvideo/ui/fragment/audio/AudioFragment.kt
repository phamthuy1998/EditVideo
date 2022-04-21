package com.thuypham.ptithcm.editvideo.ui.fragment.audio

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentAudioBinding
import com.thuypham.ptithcm.editvideo.extension.goBack
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment

class AudioFragment : BaseFragment<FragmentAudioBinding>(R.layout.fragment_audio) {

    private var resultPath: String? = null
    private var mMediaPlayer: MediaPlayer? = null

    override fun setupView() {
        setupToolbar()
        resultPath = arguments?.getString(HomeFragment.RESULT_PATH)
        binding.tvOutputPath.text = resultPath

        setupAudio()
    }

    private fun setupAudio() {
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        mMediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(resultPath))

        mMediaPlayer?.let {
            it.setOnCompletionListener {

            }
            it.start()
            it.isLooping = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.extract_audio_title))
        setLeftBtn(R.drawable.ic_close) {
            goBack()
        }
    }

}