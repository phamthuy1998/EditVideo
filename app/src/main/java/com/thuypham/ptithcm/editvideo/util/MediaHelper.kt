package com.thuypham.ptithcm.editvideo.util

import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.thuypham.ptithcm.editvideo.extension.milliSecondToDateFormat
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MediaHelper(private val context: Context) : IMediaHelper {
    private val contentResolver: ContentResolver = context.contentResolver

    companion object {
        const val SORT_BY_DATE = "date_added"
        const val SORT_BY_SIZE = "_size"
        const val ORDER_ASC = "ASC"
        const val ORDER_DESC = "DESC"

        private val IMAGES = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
        )

        private val AUDIOS = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
        )

        private val VIDEOS = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
        )
    }

    private var allAudioFiles: ArrayList<MediaFile> = arrayListOf()
    private var allMediaImages: ArrayList<MediaFile> = arrayListOf()
    private var allMediaVideos: ArrayList<MediaFile> = arrayListOf()

    private fun scanVideoFile(
        sortBy: String?,
        orderBy: String?
    ): ArrayList<MediaFile> {
        allMediaVideos.clear()
        val sortOrder = if (sortBy != null && orderBy != null) "$sortBy $orderBy" else null
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            VIDEOS,
            null,
            null,
            sortOrder
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path = cursor.getStringOrNull(0)
                val dateTaken = cursor.getLongOrNull(1)
                val size = cursor.getLongOrNull(2)
                var duration = cursor.getLongOrNull(3)
                val bucketName = cursor.getStringOrNull(4)
                val id = cursor.getLong(5)
                val name = cursor.getStringOrNull(6)
                if (duration == 0L) {
                    val mp = MediaPlayer.create(context, Uri.parse(path))
                    duration = mp.duration.toLong()
                    mp.release()
                }
                val videoFile = MediaFile(
                    id = id,
                    displayName = name,
                    path = path,
                    dateAdded = dateTaken,
                    duration = duration,
                    mediaType = MediaFile.MEDIA_TYPE_VIDEO,
                    size = size,
                    bucketName = bucketName,
                    dateString = dateTaken?.milliSecondToDateFormat()
                )
                allMediaVideos.add(videoFile)
            }
            cursor.close()
        }
        return allMediaVideos
    }

    private fun scanAudioFile(
        sortBy: String?,
        orderBy: String?
    ): ArrayList<MediaFile> {
        allAudioFiles.clear()
        val sortOrder = if (sortBy != null && orderBy != null) "$sortBy $orderBy" else null
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            AUDIOS,
            null,
            null,
            sortOrder
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path = cursor.getStringOrNull(0)
                val dateTaken = cursor.getLongOrNull(1)
                val size = cursor.getLongOrNull(2)
                var duration = cursor.getLongOrNull(3)
                val id = cursor.getLong(4)
                val name = cursor.getStringOrNull(5)
                if (duration == 0L) {
                    val mp = MediaPlayer.create(context, Uri.parse(path))
                    duration = mp.duration.toLong()
                    mp.release()
                }
                val videoFile = MediaFile(
                    id = id,
                    displayName = name,
                    path = path,
                    dateAdded = dateTaken,
                    duration = duration,
                    mediaType = MediaFile.MEDIA_TYPE_AUDIO,
                    size = size,
                    dateString = dateTaken?.milliSecondToDateFormat()
                )
                allAudioFiles.add(videoFile)
            }
            cursor.close()
        }
        return allAudioFiles
    }

    private fun scanImageFile(
        sortBy: String?,
        orderBy: String?
    ): ArrayList<MediaFile> {
        allMediaImages.clear()
        val sortOrder = if (sortBy != null && orderBy != null) "$sortBy $orderBy" else null
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            IMAGES,
            null,
            null,
            sortOrder
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path = cursor.getStringOrNull(0)
                val dateTaken = cursor.getLongOrNull(1)
                val size = cursor.getLongOrNull(2)
                val bucketName = cursor.getStringOrNull(3)
                val id = cursor.getLong(4)
                val name = cursor.getStringOrNull(5)

                val videoFile = MediaFile(
                    id = id,
                    displayName = name,
                    path = path,
                    dateAdded = dateTaken,
                    mediaType = MediaFile.MEDIA_TYPE_IMAGE,
                    size = size,
                    bucketName = bucketName,
                    dateString = dateTaken?.milliSecondToDateFormat()
                )
                allMediaImages.add(videoFile)
            }
            cursor.close()
        }
        return allMediaImages
    }


    override suspend fun getAllVideos(
        sortBy: String?,
        orderBy: String?,
    ): ResponseHandler<ArrayList<MediaFile>> {
        return withContext(Dispatchers.IO) {
            try {
                ResponseHandler.Success(scanVideoFile(sortBy, orderBy))
            } catch (ex: Exception) {
                ResponseHandler.Failure(ex, ex.message)
            }
        }
    }

    override suspend fun getAllImages(
        sortBy: String?,
        orderBy: String?,
    ): ResponseHandler<ArrayList<MediaFile>> {
        return withContext(Dispatchers.IO) {
            try {
                ResponseHandler.Success(scanImageFile(sortBy, orderBy))
            } catch (ex: Exception) {
                ResponseHandler.Failure(ex, ex.message)
            }
        }
    }

    override suspend fun getAllAudio(
        sortBy: String?,
        orderBy: String?
    ): ResponseHandler<ArrayList<MediaFile>> {
        return withContext(Dispatchers.IO) {
            try {
                ResponseHandler.Success(scanAudioFile(sortBy, orderBy))
            } catch (ex: Exception) {
                ResponseHandler.Failure(ex, ex.message)
            }
        }
    }
}

interface IMediaHelper {

    suspend fun getAllVideos(
        sortBy: String? = MediaHelper.SORT_BY_DATE,
        orderBy: String? = MediaHelper.ORDER_DESC
    ): ResponseHandler<ArrayList<MediaFile>>

    suspend fun getAllImages(
        sortBy: String? = MediaHelper.SORT_BY_DATE,
        orderBy: String? = MediaHelper.ORDER_DESC
    ): ResponseHandler<ArrayList<MediaFile>>

    suspend fun getAllAudio(
        sortBy: String? = MediaHelper.SORT_BY_DATE,
        orderBy: String? = MediaHelper.ORDER_DESC
    ): ResponseHandler<ArrayList<MediaFile>>

}