package com.thuypham.ptithcm.editvideo.ui.fragment.media

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentMediaBinding
import com.thuypham.ptithcm.editvideo.extension.*
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.GridDividerItemDecoration
import com.thuypham.ptithcm.editvideo.util.ItemDecoration
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel


class MediaFragment : BaseFragment<FragmentMediaBinding>(R.layout.fragment_media) {
    companion object {
        const val MEDIA_TYPE = "MEDIA_TYPE"
        const val MAX_SELECTED_COUNT = "MAX_SELECTED_COUNT"
    }

    private val mediaViewModel: MediaViewModel by sharedViewModel()

    private var mediaType: Int = MediaFile.MEDIA_TYPE_VIDEO
    private var maxSelectedCount = 1

    private val mediaAdapter: MediaAdapter by lazy {
        MediaAdapter(maxSelectedCount) { mediaFile: MediaFile -> onItemMediaClick(mediaFile) }
    }

    override fun setupLogic() {
        super.setupLogic()
        mediaType = arguments?.getInt(MEDIA_TYPE) ?: MediaFile.MEDIA_TYPE_VIDEO
        maxSelectedCount = arguments?.getInt(MAX_SELECTED_COUNT) ?: 1
        if (isStoragePermissionGranted()) {
            getMediaData()
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permission = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                Manifest.permission.READ_EXTERNAL_STORAGE
            else
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            requestPermissionLauncher.launch(permission)
            return false
        }
        return true
    }


    private fun getMediaData() {
        mediaViewModel.getMedia(mediaType)
    }

    private fun onItemMediaClick(mediaFile: MediaFile) {
        if (maxSelectedCount == 1 && mediaType == MediaFile.MEDIA_TYPE_VIDEO) {
            mediaViewModel.currentMedia.value = mediaFile
            goBack()
        } else if (maxSelectedCount == 1 && mediaType == MediaFile.MEDIA_TYPE_AUDIO) {
            mediaViewModel.audioMedia.value = mediaFile
            goBack()
        } else {
            mediaViewModel.mediaSelected.value = mediaAdapter.getListMediaSelected()
        }
    }

    override fun setupView() {
        setupRecyclerView()
        setupToolbar()
        setupEvent()
    }

    private fun setupEvent() {
        binding.apply {
            btnUpload.setOnSingleClickListener {
                goBack()
            }
        }
    }

    private fun setupRecyclerView() {
        var padding = 0
        var spancount = 2


        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

        val yInches = metrics.heightPixels / metrics.ydpi
        val xInches = metrics.widthPixels / metrics.xdpi
        val diagonalInches = Math.sqrt((xInches * xInches + yInches * yInches).toDouble())
        if (diagonalInches >= 6.5) {
            spancount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                4
            } else {
               3
            }
        }
        padding = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            resources.getDimension(R.dimen.dimen32).toInt()
        } else {
            resources.getDimension(R.dimen.dimen16).toInt()
        }

        val dercoration = resources.getDimension(R.dimen.dimen6).toInt()
        binding.apply {
            rvMedia.apply {
                (layoutParams as ViewGroup.MarginLayoutParams).setMargins(padding, 0, padding, 0)
//                setPadding(padding, 0, padding, 0)
                layoutManager = GridLayoutManager(requireContext(), spancount)

            }
//            rvMedia.addItemDecoration(object : RecyclerView.ItemDecoration() {
//                override fun getItemOffsets(
//                    outRect: Rect,
//                    view: View,
//                    parent: RecyclerView,
//                    state: RecyclerView.State
//                ) {
//
//                    outRect.left = dercoration
//                    outRect.right = dercoration
//                    outRect.bottom = dercoration
//                    outRect.top = dercoration
//                }
//            })

            val verDivider = DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.HORIZONTAL
            )
            val horDivider = DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
            val verDrawable=  ContextCompat.getDrawable(requireContext(),R.drawable.ver_divider)
            val horDrawable= ContextCompat.getDrawable(requireContext(),R.drawable.ver_divider)
//            rvMedia.addItemDecoration(GridDividerItemDecoration(verDrawable!!, horDrawable!!, spancount))

            horDivider.setDrawable(horDrawable!!)
            verDivider.setDrawable(verDrawable!!)
//            rvMedia.addItemDecoration(horDivider)
//            rvMedia.addItemDecoration(verDivider)


            rvMedia.addItemDecoration(ItemDecoration(dercoration, spancount))


            rvMedia.adapter = mediaAdapter
            mediaAdapter.setCanSelected(mediaType == MediaFile.MEDIA_TYPE_IMAGE)
        }
    }

    private fun setupToolbar() {
        updateHeaderTitle(0)
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
    }

    private fun updateHeaderTitle(size: Int) {
        val title = when (size) {
            0 -> {
                binding.btnUpload.gone()
                when (mediaType) {
                    MediaFile.MEDIA_TYPE_VIDEO -> getString(R.string.title_media_video)
                    MediaFile.MEDIA_TYPE_IMAGE -> getString(R.string.title_media_image)
                    MediaFile.MEDIA_TYPE_AUDIO -> getString(R.string.title_media_audio)
                    else -> getString(R.string.title_media_video)
                }
            }
            1 -> {
                binding.btnUpload.show()
                when (mediaType) {
                    MediaFile.MEDIA_TYPE_VIDEO -> getString(R.string.video_selected)
                    MediaFile.MEDIA_TYPE_IMAGE -> getString(R.string.image_selected)
                    MediaFile.MEDIA_TYPE_AUDIO -> getString(R.string.audio_selected)
                    else -> getString(R.string.video_selected)
                }

            }
            else -> {
                binding.btnUpload.show()
                when (mediaType) {
                    MediaFile.MEDIA_TYPE_VIDEO -> getString(R.string.videos_selected, size)
                    MediaFile.MEDIA_TYPE_IMAGE -> getString(R.string.images_selected, size)
                    MediaFile.MEDIA_TYPE_AUDIO -> getString(R.string.audios_selected, size)
                    else -> getString(R.string.video_selected)
                }
            }
        }
        setToolbarTitle(title)
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        mediaViewModel.mediaFiles.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Success -> {
                    hideLoading()
                    val data = result.data

                    if (data.isNullOrEmpty()) {
                        binding.layoutEmptyMedia.root.show()
                    } else {
                        binding.layoutEmptyMedia.root.gone()
                        mediaAdapter.submitList(data)
                        if (maxSelectedCount > 1)
                            mediaAdapter.setListSelected(mediaViewModel.mediaSelected.value)
                    }
                }
                is ResponseHandler.Failure -> {
                    hideLoading()

                }
                else -> {

                }
            }
        }
        if (maxSelectedCount > 1) {
            mediaViewModel.mediaSelected.observe(viewLifecycleOwner) {
                it?.size?.let { it1 -> updateHeaderTitle(it1) }
            }
        } else {
            updateHeaderTitle(0)
        }

        val mediaUri = when (mediaType) {
            MediaFile.MEDIA_TYPE_AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            MediaFile.MEDIA_TYPE_IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        requireContext().contentResolver.registerContentObserver(
            mediaUri, true,
            mediaChangeObserver
        )
    }

    private val mediaChangeObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            getMediaData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().contentResolver.unregisterContentObserver(mediaChangeObserver)
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getMediaData()
            } else {
                showSnackBar(getString(R.string.external_permission_denied))
            }
        }
}