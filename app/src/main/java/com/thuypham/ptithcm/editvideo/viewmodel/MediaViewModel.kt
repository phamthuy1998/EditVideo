package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.IMediaHelper
import kotlinx.coroutines.launch

class MediaViewModel(private val mediaHelper: IMediaHelper) : ViewModel() {

    val mediaFiles = MutableLiveData<ResponseHandler<ArrayList<MediaFile>>>()
    val mediaSelected = MutableLiveData<ArrayList<MediaFile>>()
    val currentMedia = MutableLiveData<MediaFile>()
    val resultUrl: String = ""

    fun getMedia(mediaType: Int) = viewModelScope.launch {
        mediaFiles.value = ResponseHandler.Loading
        val result = when (mediaType) {
            MediaFile.MEDIA_TYPE_IMAGE -> mediaHelper.getAllImages()
            MediaFile.MEDIA_TYPE_VIDEO -> mediaHelper.getAllVideos()
            MediaFile.MEDIA_TYPE_AUDIO -> mediaHelper.getAllAudio()
            else -> mediaHelper.getAllVideos()
        }
        when (result) {
            is ResponseHandler.Failure -> {
                mediaFiles.value = result
            }
            is ResponseHandler.Success -> {
                if (mediaSelected.value.isNullOrEmpty()) {
                    mediaFiles.value = result
                } else {
                    val tempList = arrayListOf<MediaFile>()
                    result.data.forEach { mediaFile ->
                        mediaSelected.value!!.forEach { selected ->
                            if (mediaFile.id == selected.id) {
                                mediaFile.isSelected = true
                                tempList.add(mediaFile)
                            }
                        }
                    }
                    mediaSelected.value = tempList
                    mediaFiles.value = result
                }
            }
            else -> {

            }
        }
    }

    fun deleteFile(url: String) {

    }

}