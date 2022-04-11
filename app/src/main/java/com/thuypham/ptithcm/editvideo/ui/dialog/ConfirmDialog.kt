package com.thuypham.ptithcm.editvideo.ui.dialog

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseDialogFragment
import com.thuypham.ptithcm.editvideo.databinding.DialogConfirmBinding
import com.thuypham.ptithcm.editvideo.extension.getScreenWidth
import kotlin.math.roundToInt


class ConfirmDialog(
    private val title: String? = null,
    private val msg: String? = null,
    private val okMsg: String? = null,
    private val cancelMsg: String? = null,
    private val okTextColor: Int? = null,
    private val isShowCancelMsg: Boolean? = false,
    private val onConfirmClick: (() -> Unit)? = null,
    private val onCancelClick: (() -> Unit)? = null,
) : BaseDialogFragment<DialogConfirmBinding>(R.layout.dialog_confirm) {
    companion object {
        const val TAG = "CONFIRM_DIALOG_TAG"
    }

    override fun setupView() {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        binding.apply {
            container.layoutParams.width = (getScreenWidth() * 0.8).roundToInt()
            btnCancel.setOnClickListener {
                onCancelClick?.invoke()
                dismiss()
            }
            btnConfirm.setOnClickListener {
                dismiss()
                onConfirmClick?.invoke()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.apply {
            title?.let {
                tvDialogTitle.text = it
            }
            msg?.let {
                tvDescription.text = it
            }

            okMsg?.let {
                btnConfirm.text = it
            }

            btnCancel.isVisible = isShowCancelMsg ?: false
            viewCenterButton.isVisible = isShowCancelMsg ?: false
            cancelMsg?.let {
                btnCancel.text = it
            }

            okTextColor?.let {
                btnConfirm.isAllCaps = false
                btnCancel.isAllCaps = false
                btnConfirm.setTextColor(ContextCompat.getColor(requireContext(), it))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }
}