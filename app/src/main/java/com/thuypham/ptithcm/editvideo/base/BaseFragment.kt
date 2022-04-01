package com.thuypham.ptithcm.editvideo.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.show

abstract class BaseFragment<T : ViewDataBinding>(private val layoutId: Int) : Fragment() {

    lateinit var viewbinding: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewbinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return viewbinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewbinding.apply {
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
        }
        setupLogic()
        setupView()
        setupDataObserver()
    }

    open fun setupLogic() {}
    abstract fun setupView()
    open fun setupDataObserver() {}

    fun showLoading() {
        (requireActivity() as BaseActivity<*>).showLoading()
    }

    fun hideLoading() {
        (requireActivity() as BaseActivity<*>).hideLoading()
    }

    fun setToolbarTitle(title: String, onClick: ((View) -> Unit?)? = null) {
        viewbinding.root.findViewById<AppCompatTextView>(R.id.tvTitle).apply {
            show()
            text = title
            setOnSingleClickListener { onClick?.invoke(this) }
        }
    }

    fun setLeftBtn(iconResID: Int, onClick: ((View) -> Unit?)? = null) {
        viewbinding.root.findViewById<AppCompatImageView>(R.id.ivLeft).apply {
            show()
            setImageResource(iconResID)
            setOnSingleClickListener { onClick?.invoke(this) }
        }
    }
}