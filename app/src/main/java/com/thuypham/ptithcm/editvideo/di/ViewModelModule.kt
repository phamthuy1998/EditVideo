package com.thuypham.ptithcm.editvideo.di

import com.thuypham.ptithcm.editvideo.viewmodel.*
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { HomeViewModel() }
    viewModel { MediaViewModel(get(), get()) }
    viewModel { ResultViewModel(get()) }
    viewModel { ExtractImageViewModel() }
    viewModel { MergeImageViewModel(get()) }
}
