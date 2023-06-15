package com.thuypham.ptithcm.editvideo.extension

import android.text.TextUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter


@BindingAdapter("android:enableMarqueeForever")
fun setPaddingLeft(textView: TextView, isEnable: Boolean) {
    with(textView) {
        ellipsize = TextUtils.TruncateAt.MARQUEE
        text = "Text text text text"
        isSelected = true
        isSingleLine = true
    }
}
