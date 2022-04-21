package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.extension.toSecond
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import kotlinx.coroutines.launch

class TrimAudioViewModel(private val fFmpegHelper: FFmpegHelper) : ViewModel() {
    val trimAudioResponse = MutableLiveData<ResponseHandler<String>?>()

    fun trimAudio(
        audioPath: String, startSec: Int, endSecond: Int
    ) = viewModelScope.launch {
        trimAudioResponse.value = (ResponseHandler.Loading)
        fFmpegHelper.trimAudio(audioPath, startSec.toSecond(), endSecond.toSecond(), onSuccess = {
            trimAudioResponse.postValue(ResponseHandler.Success(it))
        }, onFail = {
            trimAudioResponse.postValue(ResponseHandler.Failure(extra = it))
        })
    }
}