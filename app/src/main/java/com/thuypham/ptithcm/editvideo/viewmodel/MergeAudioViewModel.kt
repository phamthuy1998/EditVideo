package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import kotlinx.coroutines.launch

class MergeAudioViewModel(
    private val fFmpegHelper: FFmpegHelper
) : ViewModel() {
    val mergeAudioResponse = MutableLiveData<ResponseHandler<String>?>()

    fun mergeAudio(
        audios: ArrayList<MediaFile>,
    ) = viewModelScope.launch {
        mergeAudioResponse.postValue(ResponseHandler.Loading)
        fFmpegHelper.mergeAudioFile(audios, onSuccess = {
            mergeAudioResponse.postValue(ResponseHandler.Success(it))
        }, onFail = {
            mergeAudioResponse.postValue(ResponseHandler.Failure(extra = it))
        })
    }
}