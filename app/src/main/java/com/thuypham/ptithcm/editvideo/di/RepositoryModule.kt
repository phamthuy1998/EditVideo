package com.thuypham.ptithcm.editvideo.di

import com.thuypham.ptithcm.editvideo.util.FileHelper
import com.thuypham.ptithcm.editvideo.util.IFileHelper
import com.thuypham.ptithcm.editvideo.util.IMediaHelper
import com.thuypham.ptithcm.editvideo.util.MediaHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<IMediaHelper> { MediaHelper(androidContext()) }
    single { MediaHelper(androidContext()) }

    single<IFileHelper> { FileHelper(androidContext()) }
    single { FileHelper(androidContext()) }
}
