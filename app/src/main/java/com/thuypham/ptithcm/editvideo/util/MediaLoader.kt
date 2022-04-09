package com.thuypham.ptithcm.editvideo.util

import android.content.Context
import android.database.Cursor
import androidx.loader.content.CursorLoader


class MediaLoader private constructor(context: Context) : CursorLoader(context) {

    override fun loadInBackground(): Cursor? {
        return super.loadInBackground()

    }
}