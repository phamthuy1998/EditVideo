package com.thuypham.ptithcm.editvideo.extension

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import com.thuypham.ptithcm.editvideo.R

fun View.hideKeyBoard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.hideSoftInputFromWindow(this.windowToken, 0)
}


fun View.setOnSingleClickListener(action: (View) -> Unit) {
    setOnClickListener { view ->
        view.isClickable = false
        action(view)
        view.postDelayed({
            view.isClickable = true
        }, 300L)
    }
}


fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.shakeAnim(duration: Long = 300) {
    val anim = AnimationUtils.loadAnimation(this.context, R.anim.shake_anim)
    anim.duration = duration
    startAnimation(anim)
}

fun View.showSnackBar(message: String?) {
    val snackBar = Snackbar.make(this, message ?: return, Snackbar.LENGTH_SHORT)
    snackBar.show()
}

fun View.showSnackBar(stringRes: Int) {
    val snackBar = Snackbar.make(this, this.context.getString(stringRes), Snackbar.LENGTH_SHORT)
    snackBar.show()
}


