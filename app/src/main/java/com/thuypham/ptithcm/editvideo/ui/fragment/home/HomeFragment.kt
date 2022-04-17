package com.thuypham.ptithcm.editvideo.ui.fragment.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
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
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.dialog.ConfirmDialog
import com.thuypham.ptithcm.editvideo.ui.fragment.media.MediaFragment
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel


class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    companion object {
        const val RESULT_PATH = "RESULT_PATH"
    }

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
    private var startTime = 0f
    private var endTime = 0f
    private var currentMenuId = Menu.MENU_CUT_VID


    private var shouldNavigateToResultFragment = false

    private fun onMenuClick(menu: Menu) {
        if (currentMediaFile == null) {
            showSnackBar(R.string.empty_file_msg)
            return
        }
        currentMenuId = menu.id
        shouldNavigateToResultFragment = true
        when (menu.id) {
            Menu.MENU_CUT_VID -> {
                if (startTime == 0f && endTime == currentMediaFile?.duration?.toFloat()) {
                    showDialogCutVideoConfirm()
                } else {
                    player?.stop()
                    currentMediaFile?.path?.let { mediaViewModel.cutVideo(startTime, endTime, it) }
                }
            }
            Menu.MENU_EXTRACT_IMAGES -> {
                currentMediaFile?.path?.let { mediaViewModel.extractImages(startTime, endTime, it) }
            }
            Menu.MENU_EXTRACT_AUDIO -> {
                if (isAudioPermissionGranted()) {
                    currentMediaFile?.path?.let {
                        mediaViewModel.extractAudio(startTime, endTime, it)
                    }
                }

            }
            Menu.MENU_REVERSE_VIDEO -> {

            }
            Menu.MENU_CONVERT_TO_GIF -> {
                currentMediaFile?.path?.let { mediaViewModel.convertToGift(startTime, endTime, it) }
            }
            Menu.MENU_REMOVE_AUDIO_VIDEO -> {
                currentMediaFile?.path?.let { mediaViewModel.removeAudio(it) }
            }
            else -> {

            }
        }
    }

    private fun showDialogCutVideoConfirm() {
        ConfirmDialog(
            title = getString(R.string.dialog_cut_video_title),
            msg = getString(R.string.dialog_cut_video_description),
            okMsg = getString(R.string.dialog_ok),
        ).show(parentFragmentManager, ConfirmDialog.TAG)
    }

    override fun setupView() {
        setupToolbar()
        setupRecyclerView()
        setupEvent()
        setupRangeSlider()
    }

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
        setRightBtn(R.drawable.ic_menu_more) {
            showPopupMenu(it)
        }
    }

    private fun showPopupMenu(view: View) {
        PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(R.menu.home_menu, menu)
            setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menuMergeImages -> {
                        navigateTo(R.id.home_to_mergeImages)
                    }
                    R.id.menuMergeAudio -> {

                        navigateTo(R.id.home_to_mergeImages)
                    }
                    R.id.menuMergeVideos -> {
                        navigateTo(R.id.home_to_mergeImages)
                    }
                }
                true
            })
            show()
        }
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
        mediaViewModel.editVideoResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading()
                    navigateToResultFragment(response.data)
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

    private fun navigateToResultFragment(resultPath: String) {
        if (shouldNavigateToResultFragment) {
            val destinationId = when (currentMenuId) {
                Menu.MENU_CUT_VID -> R.id.homeToResult
                Menu.MENU_EXTRACT_IMAGES -> R.id.home_to_extractImages
                Menu.MENU_EXTRACT_AUDIO -> R.id.home_to_extractAudio
                Menu.MENU_REVERSE_VIDEO -> R.id.homeToResult
                Menu.MENU_CONVERT_TO_GIF -> R.id.homeToResult
                Menu.MENU_REMOVE_AUDIO_VIDEO -> R.id.homeToResult
                else -> R.id.homeToResult
            }
            shouldNavigateToResultFragment = false
            navigateTo(destinationId, bundleOf(RESULT_PATH to resultPath))
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
                    endTime = currentMediaFile?.duration?.toFloat() ?: 0f
                    rangeSlider.valueFrom = startTime
                    rangeSlider.valueTo = endTime
                    rangeSlider.values = arrayListOf(startTime, endTime)
                    tvDurationStart.text = rangeSlider.values[0].toLong().toTimeAsHHmmSSS()
                    tvDurationEnd.text = rangeSlider.values[1].toLong().toTimeAsHHmmSSS()
                    player?.seekTo(startTime.toLong())
                } else {
                    rangeSlider.isVisible = false
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        runnable = Runnable {
            if (player?.currentPosition ?: 0 >= endTime) {
                player?.seekTo(startTime.toLong())
            }
            runnable.let { handler.postDelayed(it, 1000) }
        }
        handler = Handler()
        handler.postDelayed(runnable, 1000)
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

    private fun isAudioPermissionGranted(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return false
        }
        return true
    }

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted && currentMenuId == Menu.MENU_EXTRACT_AUDIO) {
                currentMediaFile?.path?.let { mediaViewModel.extractAudio(startTime, endTime, it) }
            } else {
                showSnackBar(getString(R.string.external_permission_denied))
            }
        }

}