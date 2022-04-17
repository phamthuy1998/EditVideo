package com.thuypham.ptithcm.editvideo.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseDialogFragment
import com.thuypham.ptithcm.editvideo.databinding.DialogEditFileNameBinding
import com.thuypham.ptithcm.editvideo.extension.getScreenWidth
import java.io.File
import kotlin.math.roundToInt

class EditFileNameDialog(
    private val filePath: String,
    private val onConfirmClick: ((newFilePath: String) -> Unit)? = null,
    private val onCancelClick: (() -> Unit)? = null,
) : BaseDialogFragment<DialogEditFileNameBinding>(R.layout.dialog_edit_file_name) {
    companion object {
        const val TAG = "EditFileNameDialog"
    }

    private var fileName: String = ""
    private var filePathOnly: String = ""
    private var fileExtension: String = ""

    override fun setupView() {
        val index = filePath.lastIndexOf("/")
        filePathOnly = filePath.substring(0, index) + "/"
        val fullName = filePath.substring(index + 1, filePath.length)
        val tempName = fullName.split(".")
        fileName = tempName.first()
        fileExtension = "." + tempName.last()

        binding.apply {
            container.layoutParams.width = (getScreenWidth() * 0.8).roundToInt()
            btnCancel.setOnClickListener {
                onCancelClick?.invoke()
                dismiss()
            }
            btnConfirm.setOnClickListener {
                handleSaveFileNameEvent()
            }

            edtFileName.setText(fileName)
        }

    }

    private fun DialogEditFileNameBinding.handleSaveFileNameEvent() {
        if (edtFileName.text.isNullOrBlank()) {
            showSnackBar(getString(R.string.edit_file_name_empty))
        } else {
            val newFilenamePath = filePathOnly + edtFileName.text + fileExtension
            if (newFilenamePath == filePath) {
                showSnackBar(R.string.edit_file_name_no_change)
                return
            }
            try {
                val fileFrom = File(filePath)
                val fileTo = File(newFilenamePath)
                if (fileTo.exists()) {
                    showSnackBar(R.string.edit_file_name_error)
                    return
                }
                val isChangeFileNameSuccess = fileFrom.renameTo(fileTo)

                if (isChangeFileNameSuccess) {
                    onConfirmClick?.invoke(newFilenamePath)
                } else {
                    showSnackBar(R.string.edit_file_name_error)
                }
            } catch (ex: Exception) {
                Log.e(TAG, ex.printStackTrace().toString())
            }
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }
}