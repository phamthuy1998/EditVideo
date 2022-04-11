package com.thuypham.ptithcm.editvideo.ui.fragment.extractimage

import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentExtractImageResultBinding
import com.thuypham.ptithcm.editvideo.extension.goBack
import com.thuypham.ptithcm.editvideo.extension.gone
import com.thuypham.ptithcm.editvideo.extension.show
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.dialog.ConfirmDialog
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.ui.fragment.imagedetail.ImageDetailDialogFragment
import com.thuypham.ptithcm.editvideo.util.SpacesItemDecoration
import com.thuypham.ptithcm.editvideo.viewmodel.ExtractImageViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class ExtractImageResultFragment :
    BaseFragment<FragmentExtractImageResultBinding>(R.layout.fragment_extract_image_result) {

    private val extractImageViewModel: ExtractImageViewModel by viewModel()
    private var folderPath: String? = null

    private val imageAdapter: ImageAdapter by lazy {
        ImageAdapter {
            onImageItemAdapterClick(it)
        }
    }

    private fun onImageItemAdapterClick(imagePath: String) {
        ImageDetailDialogFragment(imagePath).show(parentFragmentManager, ConfirmDialog.TAG)
    }

    override fun setupLogic() {
        super.setupLogic()
        folderPath = arguments?.getString(HomeFragment.RESULT_PATH)
        getData()
    }

    private fun getData() {
        extractImageViewModel.getImageExtracted(folderPath)
    }

    override fun setupView() {
        setupRecyclerView()
        setupToolbar()
        binding.swRefreshLayout.setOnRefreshListener {
            getData()
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.extract_image_title))
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
        setSubRightBtn(R.drawable.ic_delete){

        }
    }

    private fun setupRecyclerView() {
        binding.apply {
            rvImages.addItemDecoration(SpacesItemDecoration(4))
            rvImages.adapter = imageAdapter
        }
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        extractImageViewModel.imagesResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading1()
                    val data = response.data
                    if (data.isNullOrEmpty()) {
                        binding.layoutEmpty.root.show()
                    } else {
                        binding.layoutEmpty.root.gone()
                        imageAdapter.submitList(data)
                    }
                }
                is ResponseHandler.Loading -> {
                    if (!binding.swRefreshLayout.isRefreshing) {
                        showLoading()
                    }
                }
                is ResponseHandler.Failure -> {
                    response.extra?.let { showSnackBar(it) }
                    hideLoading1()
                }
                else -> {
                    hideLoading1()
                }
            }
        }
    }

    private fun hideLoading1() {
        hideLoading()
        binding.swRefreshLayout.isRefreshing = false
    }
}