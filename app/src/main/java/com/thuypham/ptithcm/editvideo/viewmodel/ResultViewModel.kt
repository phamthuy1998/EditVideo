package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FileHelper
import kotlinx.coroutines.launch

class ResultViewModel(
    private val fileHelper: FileHelper
) : ViewModel() {

    val deleteFileResponse = MutableLiveData<ResponseHandler<Boolean>>()

    fun deleteFile(url: String) = viewModelScope.launch {
        deleteFileResponse.value = ResponseHandler.Loading
    }


}