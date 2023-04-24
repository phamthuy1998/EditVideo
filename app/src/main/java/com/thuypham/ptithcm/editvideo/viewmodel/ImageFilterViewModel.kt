package com.thuypham.ptithcm.editvideo.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukesh.image_processing.ImageProcessor
import com.thuypham.ptithcm.editvideo.model.ImageFilter
import com.thuypham.ptithcm.editvideo.model.ImageFilterType
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.getFilterName
import com.thuypham.ptithcm.editvideo.util.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.log


class ImageFilterViewModel(private val fileHelper: FileHelper) : ViewModel() {
    var imageMedia: MediaFile? = null
    var imageFilterItemsLiveData: MutableLiveData<ArrayList<ImageFilter>> = MutableLiveData()
    private val imageFilters = arrayListOf<ImageFilter>()

    fun getImageFilterList(_imageMedia: MediaFile?, context: Context?) {
        imageMedia = _imageMedia
        if (imageMedia == null || context == null) return
        viewModelScope.launch(Dispatchers.IO) {
            imageFilters.clear()

            val filePath = imageMedia!!.path ?: return@launch
            val bitmap = fileHelper.getBitmapFromFile(filePath)
            val processor = ImageProcessor()

            addImageFilter(bitmap, filePath,  context, ImageFilterType.FILTER0)

            val bitmapFilter = processor.tintImage(bitmap, 90)
            addImageFilter(bitmapFilter, filePath,  context, ImageFilterType.FILTER1)

            val bitmapFilter1 = processor.applyGaussianBlur(bitmap)
            addImageFilter(bitmapFilter1, filePath,  context, ImageFilterType.FILTER2)

            val bitmapFilter2 = processor.createSepiaToningEffect(bitmap, 1, 2.0, 1.0, 5.0)
            addImageFilter(bitmapFilter2, filePath,  context, ImageFilterType.FILTER3)

            val bitmapFilter3 = processor.applySaturationFilter(bitmap, 3)
            addImageFilter(bitmapFilter3, filePath,  context, ImageFilterType.FILTER4)

            val bitmapFilter4 = processor.applySnowEffect(bitmap)
            addImageFilter(bitmapFilter4, filePath,  context, ImageFilterType.FILTER5)

            val bitmapFilter5 = processor.doGreyScale(bitmap,)
            addImageFilter(bitmapFilter5, filePath,  context, ImageFilterType.FILTER6)

            val bitmapFilter6 = processor.engrave(bitmap,)
            addImageFilter(bitmapFilter6, filePath,  context, ImageFilterType.FILTER7)

            val bitmapFilter7 = processor.createContrast(bitmap,1.5)
            addImageFilter(bitmapFilter7, filePath,  context, ImageFilterType.FILTER7)

            Log.d("TAG", "getImageFilterList: imageFilters:$imageFilters")
            imageFilterItemsLiveData.postValue(imageFilters)
        }

    }

    private fun addImageFilter(bitmap: Bitmap, filePath: String, context: Context, imageFilterType: ImageFilterType) {
        val imageFilter = ImageFilter(bitmap, filePath, getFilterName(context, imageFilterType), imageFilterType)
        imageFilters.add(imageFilter)
    }

}