package com.thuypham.ptithcm.editvideo.model

import android.content.Context
import android.graphics.Bitmap
import com.thuypham.ptithcm.editvideo.R

data class ImageFilter(
    var bitmap: Bitmap,
    var filePath: String,
    var filterName: String,
    var filterType: ImageFilterType
) {
}

fun getFilterName(context: Context, filterType: ImageFilterType): String {
    return when (filterType) {
        ImageFilterType.FILTER0 -> context.getString(R.string.image_filter_0)
        ImageFilterType.FILTER1 -> context.getString(R.string.image_filter_1)
        ImageFilterType.FILTER2 -> context.getString(R.string.image_filter_3)
        ImageFilterType.FILTER3 -> context.getString(R.string.image_filter_3)
        ImageFilterType.FILTER4 -> context.getString(R.string.image_filter_4)
        ImageFilterType.FILTER5 -> context.getString(R.string.image_filter_5)
        ImageFilterType.FILTER6 -> context.getString(R.string.image_filter_6)
        ImageFilterType.FILTER7 -> context.getString(R.string.image_filter_7)
        ImageFilterType.FILTER8 -> context.getString(R.string.image_filter_8)
        else -> context.getString(R.string.image_filter_0)
    }
}

enum class ImageFilterType {
    FILTER0,
    FILTER1,
    FILTER2,
    FILTER3,
    FILTER4,
    FILTER5,
    FILTER6,
    FILTER7,
    FILTER8,

}