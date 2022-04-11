package com.thuypham.ptithcm.editvideo.extension

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController


fun Fragment.navigateTo(destination: Int, bundle: Bundle? = null, option: NavOptions? = null) {
    try {
        findNavController().navigate(destination, bundle, option)
    } catch (ex: Exception) {
        Log.d(this.tag, ex.printStackTrace().toString())
    }
}

fun Fragment.goBack() {
    try {
        findNavController().popBackStack()
    } catch (ex: Exception) {
        Log.d(this.tag, ex.printStackTrace().toString())
    }
}


fun Fragment.navigateToWithAction(action: NavDirections) {
    try {
        findNavController().navigate(action)
    } catch (ex: Exception) {
        Log.d(this.tag, ex.printStackTrace().toString())
    }
}


fun Fragment.canGoBack(): Boolean {
    return findNavController().previousBackStackEntry != null
}

fun Fragment.getScreenWidth(): Int {
    val displayMetrics = DisplayMetrics()
    requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

fun Fragment.getScreenHeight(): Int {
    val displayMetrics = DisplayMetrics()
    requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}
