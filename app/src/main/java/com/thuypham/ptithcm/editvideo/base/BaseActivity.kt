package com.thuypham.ptithcm.editvideo.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.thuypham.ptithcm.editvideo.extension.hideKeyBoard
import com.thuypham.ptithcm.editvideo.ui.dialog.ProgressDialog

abstract class BaseActivity<T : ViewDataBinding>(private val layoutId: Int) : AppCompatActivity() {

    lateinit var binding: T
    lateinit var dialog: Dialog
    var isAutoHideKeyboard = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("thuyyy", "bonCreate")
        binding = DataBindingUtil.setContentView(this, layoutId)
        binding.lifecycleOwner = this
        setupLogic()
        setupView()
        setupDataObserver()

        dialog = ProgressDialog.progressDialog(this)
    }

    open fun setupLogic() {}
    abstract fun setupView()
    open fun setupDataObserver() {}

    override fun finish() {
        binding.root.hideKeyBoard()
        super.finish()
    }


    fun hideLoading() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun showLoading() {
        dialog.show()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val ret = super.dispatchTouchEvent(event)
        event?.let { ev ->
            if (ev.action == MotionEvent.ACTION_UP) {
                currentFocus?.let { view ->
                    if (view is EditText) {
                        val touchCoordinates = IntArray(2)
                        view.getLocationOnScreen(touchCoordinates)
                        val x: Float = ev.rawX + view.getLeft() - touchCoordinates[0]
                        val y: Float = ev.rawY + view.getTop() - touchCoordinates[1]
                        //If the touch position is outside the EditText then we hide the keyboard
                        if (x < view.getLeft() || x >= view.getRight() || y < view.getTop() || y > view.getBottom()) {
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view.windowToken, 0)
                            view.clearFocus()
                        }
                    }
                }
            }
        }
        return ret
    }

    protected open fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        hideKeyboard()
    }


//
//    /**
//     * Overrides the pending Activity transition by performing the "Enter" animation.
//     */
//    private fun overridePendingTransitionEnter() {
//        if (isBottomUpActivity()) {
//            overridePendingTransition(R.anim.slide_up, R.anim.stay)
//        } else {
//            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
//        }
//    }
//
//    /**
//     * Overrides the pending Activity transition by performing the "Exit" animation.
//     */
//    private fun overridePendingTransitionExit() {
//        if (isBottomUpActivity()) {
//            overridePendingTransition(R.anim.stay, R.anim.slide_down)
//        } else {
//            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
//        }
//    }

}