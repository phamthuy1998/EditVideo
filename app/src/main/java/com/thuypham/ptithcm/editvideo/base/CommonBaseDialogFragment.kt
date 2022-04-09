package com.thuypham.ptithcm.editvideo.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment

abstract class CommonBaseDialogFragment<T : ViewDataBinding>(private val layoutId: Int) :
    DialogFragment() {

    var onFinishLoading: (() -> Unit)? = null
    open val isFullScreen: Boolean = true

    lateinit var binding: T

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFullScreen) {
            setStyle(STYLE_NORMAL, android.R.style.Theme_Translucent_NoTitleBar)
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupView()
            onFinishLoading?.invoke()
        } catch (e: Exception) {
            Log.e(this::javaClass.name, e.printStackTrace().toString())
        }
    }

    open fun setupLogic() {}
    open fun setupView(){}
    open fun setupDataObserver() {}

    fun showLoading() {
        (requireActivity() as BaseActivity<*>).showLoading()
    }

    fun hideLoading() {
        (requireActivity() as BaseActivity<*>).hideLoading()
    }
}