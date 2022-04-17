package com.thuypham.ptithcm.editvideo.di

import com.thuypham.ptithcm.editvideo.util.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<IMediaHelper> { MediaHelper(androidContext()) }
    single { MediaHelper(androidContext()) }

    single<IFileHelper> { FileHelper(androidContext()) }
    single { FileHelper(androidContext()) }

    single { FFmpegHelper(androidContext()) }

}
