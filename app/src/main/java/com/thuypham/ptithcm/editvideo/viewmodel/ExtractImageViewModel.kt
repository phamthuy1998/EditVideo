package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ExtractImageViewModel(
) : ViewModel() {
    val imagesResponse = MutableLiveData<ResponseHandler<ArrayList<String>>>()

    fun getImageExtracted(folderPath: String?) = viewModelScope.launch(Dispatchers.IO) {
        imagesResponse.postValue(ResponseHandler.Loading)
        if (folderPath == null) {
            imagesResponse.value = ResponseHandler.Failure(extra = "Cant get image!")
            return@launch
        }
        try {
            val listImagePath = arrayListOf<String>()
            val fileDir = File(folderPath)
            var count = 0
            for (file in fileDir.walk()) {
                if (count != 0)
                    listImagePath.add(file.path)
                count++
            }
            imagesResponse.postValue(ResponseHandler.Success(listImagePath))
        } catch (ex: Exception) {
            imagesResponse.postValue(ResponseHandler.Failure(ex, ex.message))
        }
    }
}