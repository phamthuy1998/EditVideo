package com.thuypham.ptithcm.editvideo.di

import com.thuypham.ptithcm.editvideo.viewmodel.HomeViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { HomeViewModel() }
    viewModel { MediaViewModel(get()) }
}
