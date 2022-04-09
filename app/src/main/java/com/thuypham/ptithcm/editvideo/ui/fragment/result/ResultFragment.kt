package com.thuypham.ptithcm.editvideo.ui.fragment.result

import android.util.Log
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentResultBinding
import com.thuypham.ptithcm.editvideo.ui.dialog.ConfirmDialog
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ResultFragment : BaseFragment<FragmentResultBinding>(R.layout.fragment_result) {

    private val mediaViewModel: MediaViewModel by sharedViewModel()

    override fun setupView() {
        setupToolbar()
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.result))
        setRightBtn(R.drawable.ic_delete) {
            showDialogDeleteConfirm()
        }
        setSubRightBtn(R.drawable.ic_share) {

        }
        setSubRightBtn(R.drawable.ic_edit) {

        }
    }

    private fun showDialogDeleteConfirm() {
        ConfirmDialog(
            title = getString(R.string.dialog_msg_delete_title),
            msg = getString(R.string.dialog_msg_delete_project),
            cancelMsg = getString(R.string.dialog_cancel),
            isShowCancelMsg = true,
            onConfirmClick = {
                mediaViewModel.deleteFile(mediaViewModel.resultUrl)
            },
            onCancelClick = {
            }).show(
            parentFragmentManager,
            ConfirmDialog.TAG
        )
    }


}