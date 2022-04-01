package com.thuypham.ptithcm.editvideo.ui.fragment.home

import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentHomeBinding
import com.thuypham.ptithcm.editvideo.model.Menu
import com.thuypham.ptithcm.editvideo.util.SpacesItemDecoration

class HomeFragment:BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val menuAdapter : MenuAdapter by lazy {
        MenuAdapter { menu -> onMenuClick(menu) }
    }

    private fun onMenuClick(menu: Menu) {

    }

    override fun setupView() {
        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.app_name))
    }

    private fun setupRecyclerView() {
        viewbinding.apply {
//            rvMenu.addItemDecoration(SpacesItemDecoration(2))
            rvMenu.adapter = menuAdapter
            menuAdapter.submitList(MenuAdapter.listMenu)
        }
    }
}