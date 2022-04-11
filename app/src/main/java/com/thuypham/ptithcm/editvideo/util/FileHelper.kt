package com.thuypham.ptithcm.editvideo.util

import android.content.Context
import android.util.Log
import com.thuypham.ptithcm.editvideo.MainApplication
import com.thuypham.ptithcm.editvideo.model.MediaFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileHelper constructor(context: Context) : IFileHelper {
    companion object {
        const val MEDIA_FOLDER_NAME = "MEDIA_FILE"
        const val TEMP_FOLDER = "TEMP_FILE"
        const val OUTPUT_FOLDER_NAME = "EditVideo"
    }

    private val file = File(context.filesDir, MEDIA_FOLDER_NAME)
    private val fileDir = context.getDir(MEDIA_FOLDER_NAME, Context.MODE_PRIVATE)

    init {
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    override fun saveMediaFile(mediaFile: MediaFile): MediaFile {
        // If media file is not saved --> save file --> update path for MediaFile
        // else --> get existed path --> update path for MediaFile
        val isMediaFileSaved = isMediaFileSaved(mediaFile)
        if (!isMediaFileSaved.first) {

            try {
                val path = mediaFile.path
                val sourceFile = File(path)

                if (sourceFile.exists()) {

                    // Add id to name of file with format: filename_id.extension
                    val fileName = sourceFile.nameWithoutExtension
                        .plus("_${mediaFile.id}")
                        .plus(".${sourceFile.extension}")

                    val destinationFile = File(fileDir.path, fileName)

                    // Copy file from storage to internal storage app
                    sourceFile.copyTo(destinationFile, true)

                    // Update path for MediaFile
                    val newPath = destinationFile.path
                    mediaFile.path = newPath
                }
            } catch (ex: Exception) {
                Log.e(this::class.java.name, " Save file error: ${ex.message}")
                throw ex
            } catch (ex: NoSuchFileException) {
                Log.e(this::class.java.name, " Save file error: ${ex.message}")
                throw ex
            }
        } else {
            val existMediaFilePath = isMediaFileSaved.second
            mediaFile.path = existMediaFilePath
        }
        return mediaFile
    }

    override fun saveMediaFiles(mediaFiles: ArrayList<MediaFile>): ArrayList<MediaFile> {
        try {
            mediaFiles.forEach { mediaFile ->
                saveMediaFile(mediaFile)
            }
            return mediaFiles
        } catch (ex: Exception) {
            throw  ex
        }
    }

    override fun isMediaFileSaved(mediaFile: MediaFile): Pair<Boolean, String> {
        val files = fileDir.listFiles()
        val mediaId = mediaFile.id.toString()
        files?.forEach { file ->
            if (file.name.endsWith(mediaId)) {
                return Pair(true, file.path)
            }
        }
        return Pair(false, "")
    }

    override fun deleteFile(path: String): Boolean {
        // Todo: Fix Bug --> Can't delete media file in internal storage
        val file = File(path)
        var isDeleted = false
        if (file.exists()) {
            file.delete()
            isDeleted = file.canonicalFile.delete()
            if (file.exists()) {
                return MainApplication.instance.deleteFile(path)
            }
        }
        return isDeleted
    }


    override fun saveFileToApp(fileName: String, input: InputStream): Boolean {
        return try {
            val filePath = File(fileDir, fileName)
            saveFile(filePath.absolutePath, input)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e(this::class.java.name, " Save file error: ${ex.message}")
            throw ex
        }
    }

    override fun saveFileToDevice(fileName: String, input: InputStream): Boolean {
        return try {
            // Todo: Update this path
            val fileDir = MainApplication.instance.filesDir.absolutePath
            val filePath = File(fileDir, fileName)
            saveFile(filePath.absolutePath, input)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e(this::class.java.name, " Save file error: ${ex.message}")
            throw ex
        }
    }

    private fun saveFile(filePath: String, input: InputStream): Boolean {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.use { output ->
                val buffer = ByteArray(1024)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
            }
            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e(this::class.java.name, " Save file error: ${ex.message}")
            throw ex
        }
    }
}

// For media files in app specific
interface IFileHelper {

    fun saveMediaFile(mediaFile: MediaFile): MediaFile

    fun saveMediaFiles(mediaFiles: ArrayList<MediaFile>): ArrayList<MediaFile>

    /* If media files is existed, return a path of file */
    fun isMediaFileSaved(mediaFile: MediaFile): Pair<Boolean, String>

    fun deleteFile(path: String): Boolean

    fun saveFileToApp(fileName: String, input: InputStream): Boolean

    fun saveFileToDevice(fileName: String, input: InputStream): Boolean
}