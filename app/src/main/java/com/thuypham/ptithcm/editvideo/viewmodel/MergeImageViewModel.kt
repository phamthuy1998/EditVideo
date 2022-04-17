package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import kotlinx.coroutines.launch

class MergeImageViewModel(
    private val fFmpegHelper: FFmpegHelper,
) : ViewModel() {
    val mergeImageResponse = MutableLiveData<ResponseHandler<String>?>()

    fun mergeImageToVideo(
        images: ArrayList<MediaFile>,
        secondPerImg: Int,
        audioPath: String? = null
    ) = viewModelScope.launch {
        mergeImageResponse.postValue(ResponseHandler.Loading)
        fFmpegHelper.executeImagesToVideo(images, secondPerImg, audioPath, onSuccess = {
            mergeImageResponse.postValue(ResponseHandler.Success(it))
        }, onFail = {
            mergeImageResponse.postValue(ResponseHandler.Failure(extra = it))
        })
    }
}