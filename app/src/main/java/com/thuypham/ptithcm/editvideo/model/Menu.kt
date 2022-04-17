package com.thuypham.ptithcm.editvideo.model

data class Menu(
    var id: Int,
    var name: String? = null,
    var iconRes: Int? = null,
) {
    companion object {
        const val MENU_CUT_VID = 1
        const val MENU_EXTRACT_IMAGES = 2
        const val MENU_EXTRACT_AUDIO = 3
        const val MENU_REVERSE_VIDEO = 4
        const val MENU_CONVERT_TO_GIF = 5
        const val MENU_REMOVE_AUDIO_VIDEO = 6
    }
}