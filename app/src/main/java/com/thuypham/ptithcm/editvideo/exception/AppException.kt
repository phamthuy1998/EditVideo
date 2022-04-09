package com.thuypham.ptithcm.editvideo.exception

import java.lang.Exception

sealed class AppException : Exception() {
    object Failed : AppException()
    object InvalidParam : AppException()
    object NoNetwork: AppException()
    object UnsolvedHost: AppException()
    object NotFound: AppException()
    object BridgeNotFound: AppException()
    object Unknown: AppException()
    object FirebaseException: AppException()
    object RetryConnectBridge: AppException()
    object ConnectException: AppException()
}