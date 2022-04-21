package com.thuypham.ptithcm.editvideo.ui.fragment.mergeimgvid

import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentMergeImageVideoBinding
import com.thuypham.ptithcm.editvideo.extension.goBack

class MergeImageVideoFragment: BaseFragment<FragmentMergeImageVideoBinding>(R.layout.fragment_merge_image_video) {
    override fun setupView() {
        setupToolbar()
    }

    private fun setupToolbar() {
        setToolbarTitle(R.string.menu_merge_img_video)
        setLeftBtn(R.drawable.ic_back){
            goBack()
        }
    }
}