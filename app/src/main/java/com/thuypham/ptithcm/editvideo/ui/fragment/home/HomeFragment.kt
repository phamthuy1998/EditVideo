package com.thuypham.ptithcm.editvideo.ui.fragment.home

import android.annotation.SuppressLint
import android.os.Handler
import android.util.Log
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import com.google.android.material.slider.RangeSlider
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentHomeBinding
import com.thuypham.ptithcm.editvideo.extension.gone
import com.thuypham.ptithcm.editvideo.extension.navigateTo
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.toTimeAsHHmmSSS
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.Menu
import com.thuypham.ptithcm.editvideo.ui.fragment.media.MediaFragment
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel


class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val mediaViewModel: MediaViewModel by sharedViewModel()

    private val menuAdapter: MenuAdapter by lazy {
        MenuAdapter { menu -> onMenuClick(menu) }
    }

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private var currentMediaFile: MediaFile? = null

    private var mediaItem: MediaItem? = null
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler

    private fun onMenuClick(menu: Menu) = when (menu.id) {
        Menu.MENU_CUT_VID -> {

        }
        Menu.MENU_EXTRACT_IMAGES -> {

        }
        Menu.MENU_EXTRACT_AUDIO -> {

        }
        Menu.MENU_REVERSE_VIDEO -> {

        }
        Menu.MENU_CONVERT_TO_GIF -> {

        }
        Menu.MENU_SPLIT_VIDEO -> {

        }
        else -> {

        }
    }

    override fun setupView() {
        setupToolbar()
        setupRecyclerView()
        setupEvent()
        setupRangeSlider()
    }

    private var startTime = 0f
    private var endTime = 0f
    private fun setupRangeSlider() {
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
                    startTime = rangeSlider.values[0]
                    endTime = rangeSlider.values[1]
                    tvDurationStart.text = startTime.toLong().toTimeAsHHmmSSS()
                    tvDurationEnd.text = endTime.toLong().toTimeAsHHmmSSS()
                    player?.seekTo(startTime.toLong())
                }

            })
        }
    }

    private fun setupEvent() {
        binding.btnUploadVideo.setOnSingleClickListener {
            navigateTo(
                R.id.media,
                bundleOf(
                    MediaFragment.MEDIA_TYPE to MediaFile.MEDIA_TYPE_VIDEO,
                    MediaFragment.MAX_SELECTED_COUNT to 1
                )
            )
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.app_name))
    }

    private fun setupRecyclerView() {
        binding.apply {
            rvMenu.adapter = menuAdapter
            menuAdapter.submitList(MenuAdapter.listMenu)
        }
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        mediaViewModel.currentMedia.observe(viewLifecycleOwner) {
            binding.apply {
                layoutEmptyVideo.gone()
                currentMediaFile = it
                setMediaItem()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    private fun setMediaItem() {
        mediaItem = currentMediaFile?.path?.let { MediaItem.fromUri(it) }
        player?.setMediaItem(mediaItem!!)
        player?.prepare()
        binding.apply {
            videoView.hideController()
            try {
                if (currentMediaFile?.duration ?: 0 > 0) {
                    rangeSlider.isVisible = true
                    val valueTo = currentMediaFile?.duration?.toFloat() ?: 0f
                    rangeSlider.valueTo = valueTo
                    rangeSlider.values = arrayListOf(0f, valueTo)
                    tvDurationStart.text = rangeSlider.values[0].toLong().toTimeAsHHmmSSS()
                    tvDurationEnd.text = rangeSlider.values[1].toLong().toTimeAsHHmmSSS()
                } else {
                    rangeSlider.isVisible = false
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        runnable = Runnable {}
        handler = Handler()

        handler.postDelayed(Runnable {
            if (player?.currentPosition ?: 0 >= endTime) {
                player?.seekTo(startTime.toLong())
            }
            runnable.let { handler.postDelayed(it, 1000) }
        }.also { runnable = it }, 1000)
    }

    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(requireContext()).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = ExoPlayer.Builder(requireContext())
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer

                mediaItem?.let { exoPlayer.setMediaItem(it) }
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        player = null
    }

    private val playbackStateListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(this::class.java.name, "changed state to $stateString")
        }
    }
}