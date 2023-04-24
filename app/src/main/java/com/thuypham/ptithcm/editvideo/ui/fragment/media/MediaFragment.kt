package com.thuypham.ptithcm.editvideo.ui.fragment.media

import android.Manifest
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentMediaBinding
import com.thuypham.ptithcm.editvideo.extension.goBack
import com.thuypham.ptithcm.editvideo.extension.gone
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.show
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.SpacesItemDecoration
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

    var isSelectAll = false
    private fun setupEvent() {
        binding.apply {
            btnUpload.setOnSingleClickListener {
                isSelectAll = !isSelectAll
                val newList = arrayListOf<MediaFile>()
                tempList.onEach {
                    it.isSelected = isSelectAll
                    val media = MediaFile().apply {
                        id = it.id
                        path = it.path
                        dateAdded = it.dateAdded
                        duration = it.duration
                        thumbnail = it.thumbnail
                        bucketName = it.bucketName
                        mediaType = it.mediaType
                        isSelected = it.isSelected
                    }
                    newList.add(media)
                }
                mediaAdapter.submitList(newList)


//                mediaAdapter.selectAll(isSelectAll)
//                mediaViewModel.mediaSelected.value = mediaAdapter.getListMediaSelected()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.apply {
            rvMedia.addItemDecoration(SpacesItemDecoration(4))
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
        binding.btnUpload.show()
        val title = when (size) {
            0 -> {
//                binding.btnUpload.gone()
                when (mediaType) {
                    MediaFile.MEDIA_TYPE_VIDEO -> getString(R.string.title_media_video)
                    MediaFile.MEDIA_TYPE_IMAGE -> getString(R.string.title_media_image)
                    MediaFile.MEDIA_TYPE_AUDIO -> getString(R.string.title_media_audio)
                    else -> getString(R.string.title_media_video)
                }
            }
            1 -> {
//                binding.btnUpload.show()
                when (mediaType) {
                    MediaFile.MEDIA_TYPE_VIDEO -> getString(R.string.video_selected)
                    MediaFile.MEDIA_TYPE_IMAGE -> getString(R.string.image_selected)
                    MediaFile.MEDIA_TYPE_AUDIO -> getString(R.string.audio_selected)
                    else -> getString(R.string.video_selected)
                }

            }
            else -> {
//                binding.btnUpload.show()
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

    var tempList = arrayListOf<MediaFile>()
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
                    tempList = data

                    if (data.isNullOrEmpty()) {
                        binding.layoutEmptyMedia.root.show()
                    } else {
                        binding.layoutEmptyMedia.root.gone()
                        Log.d(TAG, "setupDataObserver: submit list")
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