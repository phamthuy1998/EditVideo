package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import kotlinx.coroutines.launch

class MergeVideoViewModel(
    private val fFmpegHelper: FFmpegHelper
) : ViewModel() {
    val mergeVideoResponse = MutableLiveData<ResponseHandler<String>?>()

    fun mergeVideo(
        videoMedias: ArrayList<MediaFile>,
        isRemoveAudio: Boolean,
        secondPerVideo: Int? = null,
        audioPath: String?
    ) = viewModelScope.launch {
        mergeVideoResponse.value = (ResponseHandler.Loading)
        fFmpegHelper.executeMergeVideo(videoMedias,isRemoveAudio, secondPerVideo, audioPath,  onSuccess = {
            mergeVideoResponse.postValue(ResponseHandler.Success(it))
        }, onFail = {
            mergeVideoResponse.postValue(ResponseHandler.Failure(extra = it))
        })

    }
}