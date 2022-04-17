package com.thuypham.ptithcm.editvideo.ui.fragment.result

import android.util.Log
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentResultBinding
import com.thuypham.ptithcm.editvideo.extension.goBack
import com.thuypham.ptithcm.editvideo.extension.shareImageToOtherApp
import com.thuypham.ptithcm.editvideo.ui.dialog.ConfirmDialog
import com.thuypham.ptithcm.editvideo.ui.dialog.EditFileNameDialog
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.viewmodel.ResultViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class ResultFragment : BaseFragment<FragmentResultBinding>(R.layout.fragment_result) {


    private val resultViewModel: ResultViewModel by viewModel()

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private var resultPath: String? = null
    private var isGifFile: Boolean = false

    override fun setupLogic() {
        super.setupLogic()
        resultPath = arguments?.getString(HomeFragment.RESULT_PATH)
        isGifFile = resultPath?.contains("gif") ?: false
    }

    override fun setupView() {
        setupToolbar()
        binding.apply {
            tvOutputPath.text = resultPath
            ivImage.isVisible = isGifFile
            if (isGifFile) {
                Glide.with(this@ResultFragment).asGif()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .load(File(resultPath))
                    .into(ivImage);
            }
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.result))
        setRightBtn(R.drawable.ic_delete) {
            showDialogDeleteConfirm()
        }
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
        setSubRightBtn(R.drawable.ic_share) {
            resultPath?.let { it1 -> shareImageToOtherApp(it1) }
        }
        setSubRight2Btn(R.drawable.ic_edit) {
            showDialogEditFile()
        }
    }

    private fun showDialogEditFile() {
        EditFileNameDialog(
            resultPath ?: return,
            {
                resultPath = it
                binding.tvOutputPath.text = resultPath
            }).show(parentFragmentManager, EditFileNameDialog.TAG)
    }

    private fun showDialogDeleteConfirm() {
        ConfirmDialog(
            title = getString(R.string.dialog_msg_delete_title),
            msg = getString(R.string.dialog_msg_delete_project),
            cancelMsg = getString(R.string.dialog_cancel),
            isShowCancelMsg = true,
            onConfirmClick = {
                val file = File(resultPath)
                if (file.exists()) file.delete()
                goBack()
            },
        ).show(
            parentFragmentManager,
            ConfirmDialog.TAG
        )
    }

    override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT <= 23 || player == null) && !isGifFile) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23 && !isGifFile) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentMediaItemIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        player = null
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
                resultPath?.let { exoPlayer.setMediaItem(MediaItem.fromUri(it)) }
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                exoPlayer.prepare()
            }
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