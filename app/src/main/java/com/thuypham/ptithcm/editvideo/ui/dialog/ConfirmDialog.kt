package com.thuypham.ptithcm.editvideo.ui.dialog

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.CommonBaseDialogFragment
import com.thuypham.ptithcm.editvideo.databinding.DialogConfirmBinding


class ConfirmDialog(
    private val title: String? = null,
    private val msg: String? = null,
    private val okMsg: String? = null,
    private val cancelMsg: String? = null,
    private val okTextColor: Int? = null,
    private val isShowCancelMsg: Boolean? = false,
    private val onConfirmClick: (() -> Unit)? = null,
    private val onCancelClick: (() -> Unit)? = null,
) : CommonBaseDialogFragment<DialogConfirmBinding>(R.layout.dialog_confirm) {
    companion object {
        const val TAG = "CONFIRM_DIALOG_TAG"
    }

    override fun setupView() {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        binding.apply {
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
                btnConfirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)

                tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
                tvDialogTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)

                val keyWord = "Hue Bridge"
                val spannableString = SpannableString(tvDescription.text)
                val backgroundColorSpan = spannableString.getSpans(
                    0, spannableString.length,
                    BackgroundColorSpan::class.java
                )
                for (bgSpan in backgroundColorSpan) {
                    spannableString.removeSpan(bgSpan)
                }
                var indexOfKeyWord: Int = spannableString.toString().indexOf(keyWord)
                while (indexOfKeyWord > 0) {
                    spannableString.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        ), indexOfKeyWord,
                        indexOfKeyWord + keyWord.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    indexOfKeyWord =
                        spannableString.toString().indexOf(keyWord, indexOfKeyWord + keyWord.length)
                }

                tvDescription.text = spannableString

            }
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)

    }
}