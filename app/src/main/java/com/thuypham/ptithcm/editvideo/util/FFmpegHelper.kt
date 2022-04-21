package com.thuypham.ptithcm.editvideo.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.thuypham.ptithcm.editvideo.extension.toSecond
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ScaleType
import com.thuypham.ptithcm.editvideo.util.FileHelper.Companion.OUTPUT_FOLDER_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*


class FFmpegHelper constructor(
    private val context: Context,
) {
    private var outputDir: File
    private var outputAudioDir: File
    private val tempDir = context.getDir(FileHelper.TEMP_FOLDER, Context.MODE_PRIVATE)

    init {
        val tempDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        outputDir = File(tempDir, OUTPUT_FOLDER_NAME)
        outputAudioDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        if (!outputDir.exists()) outputDir.mkdir()
        if (!tempDir.exists()) tempDir.createNewFile()
    }

    companion object {
        const val FRAME_RATE: Int = 5
        const val DEFAULT_WIDTH: Int = 1280
        const val DEFAULT_HEIGHT: Int = 720
        const val DEFAULT_SECOND: Int = 2

        const val TAG = "FFmpegHelper"
    }

    suspend fun cutVideo(
        startMs: Int, endMs: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputVideoPath("cut_video")
            val complexCommand = arrayOf(
                "-ss",
                "" + startMs / 1000,
                "-y",
                "-i",
                filePath,
                "-t",
                "" + (endMs - startMs) / 1000,
                "-vcodec",
                "mpeg4",
                "-b:v",
                "2097152",
                "-b:a",
                "48000",
                "-ac",
                "2",
                "-ar",
                "22050",
//                "-preset",
//                "ultrafast",
                outputPath
            )
            val complexCommand1 = arrayOf(
                "-y",
                "-i",
                filePath,
                "-ss",
                "" + startMs / 1000,
                "-t",
                "" + (endMs - startMs) / 1000,
                "-c",
                "copy",
                "-preset",
                "ultrafast",
                outputPath
            )

            executeCommand(complexCommand1, {
                onSuccess?.invoke(outputPath)
            }, {
                val fileImageVideo = File(outputPath)
                if (fileImageVideo.exists()) fileImageVideo.delete()
                onFail?.invoke(it)
            })
        }
    }

    suspend fun extractAudio(
        startMs: Int, endMs: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputAudioPath("extract_audio")
            val complexCommand = arrayOf(
                "-y",
                "-i",
                filePath,
                "-vn",
                "-ac",
                "2",
                "-ar",
                "44100",
                "-b:a",
                "128k",
                "-f",
                "mp3",
                outputPath
            )

            executeCommand(complexCommand, {
                onSuccess?.invoke(outputPath)
            }, {
                val fileImageVideo = File(outputPath)
                if (fileImageVideo.exists()) fileImageVideo.delete()
                onFail?.invoke(it)
            })
        }
    }

    suspend fun convertVideoToGif(
        startMs: Int, endMs: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputGifPath("video_to_gif")
            val complexCommand = arrayOf(
                "-ss",
                "" + startMs / 1000,
                "-t",
                "" + (endMs - startMs) / 1000,
                "-y",
                "-i",
                filePath,
                "-preset",
                "ultrafast",
                "-vf",
                "scale=500:-1",
                "-r",
                "10",
                outputPath
            )
            executeCommand(complexCommand, {
                onSuccess?.invoke(outputPath)
            }, {
                val fileImageVideo = File(outputPath)
                if (fileImageVideo.exists()) fileImageVideo.delete()
                onFail?.invoke(it)
            })
        }
    }

    suspend fun reverseVideo(
        startMs: Int, endMs: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            cutVideo(startMs, endMs, filePath, { videoCuttedPath ->
                val outputPath = getOutputGifPath("reverse_video")
                val complexCommand = arrayOf(
                    "-ss",
                    "" + startMs / 1000,
                    "-t",
                    "" + (endMs - startMs) / 1000,
                    "-y",
                    "-i",
                    videoCuttedPath,
                    "-preset",
                    "ultrafast",
                    "-vf",
                    "scale=500:-1",
                    "-r",
                    "10",
                    outputPath
                )
                val file = File(videoCuttedPath)
                executeCommand(complexCommand, {
                    file.deleteOnExit()
                    onSuccess?.invoke(outputPath)
                }, {
                    file.deleteOnExit()
                    onFail?.invoke(it)
                })
            }, onFail)
        }
    }

    suspend fun extractImages(
        startMs: Int, endMs: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            var outputFolder = File(outputDir, "extract_images")
            var fileNo = 0
            while (outputFolder.exists()) {
                fileNo++
                outputFolder = File(outputDir, "extract_images_$fileNo")
            }
            outputFolder.mkdir()
            val imageFile = File(outputFolder, "extract_images_%03d.jpg")
            val complexCommand = arrayOf(
                "-y",
                "-i",
                filePath,
                "-an",
                "-r",
                "1",
                "-ss",
                "" + startMs / 1000,
                "-t",
                "" + (endMs - startMs) / 1000,
                imageFile.absolutePath
            )

            executeCommand(complexCommand, {
                onSuccess?.invoke(outputFolder.absolutePath)
            }, {
                val fileImageVideo = File(outputFolder.absolutePath)
                if (fileImageVideo.exists()) fileImageVideo.delete()
                onFail?.invoke(it)
            })
        }
    }

    suspend fun removeAudio(
        filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputVideoPath("remove_audio")
            val complexCommand = arrayOf(
                "-y",
                "-i",
                filePath,
                "-c",
                "copy",
                "-an",
                "-preset",
                "ultrafast",
                outputPath
            )

            executeCommand(complexCommand, {
                onSuccess?.invoke(outputPath)
            }, {
                val fileImageVideo = File(outputPath)
                if (fileImageVideo.exists()) fileImageVideo.delete()
                onFail?.invoke(it)
            })
        }
    }

    fun getCommandAddAudioToVideo(
        videoPath: String,
        audioPath: String,
        destinationFilePath: String,
    ): Array<String> {

        return arrayOf(
            "-i",
            videoPath,
            "-i",
            audioPath,
            "-filter_complex",
            "[0:1][1] amix=inputs=2:duration=shortest[a]",
            "-map",
            "0:0",
            "-map",
            "[a]",
            "-c:v",
            "copy",
            "-y",
            destinationFilePath
        )
    }

    private fun getOutputVideoPath(fileName: String): String {
        val file = File(outputDir, "${fileName}_${System.currentTimeMillis()}.mp4")
        return file.absolutePath
    }

    private fun getOutputAudioPath(fileName: String): String {
        val file = File(outputAudioDir, "${fileName}_${System.currentTimeMillis()}.mp3")
        return file.absolutePath
    }

    private fun getOutputGifPath(fileName: String): String {
        val file = File(outputDir, "${fileName}_${System.currentTimeMillis()}.gif")
        return file.absolutePath
    }

    private fun InputStream.toFile(path: String) {
        File(path).outputStream().use { this.copyTo(it) }
    }

    fun cmdMergeAudioWithVideo(
        videoPath: String,
        audioPath: String?,
        outputPath: String
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-y")
            add("-i")
            add(videoPath)
            add("-i")
            audioPath?.let { add(audioPath) }
            add("-c:v")
            add("copy")
            add("-map")
            add("0:v")
            add("-map")
            add("1:a")
            add("-shortest")
            add("-preset")
            add("ultrafast")
            add(outputPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    private fun cmdImagesToVideo(
        imageTextPath: String,
        outputPath: String,
        videoWidth: Int? = DEFAULT_WIDTH,
        videoHeight: Int? = DEFAULT_HEIGHT,
        audioPath: String? = null,
    ): Array<String> {
        val cmd: ArrayList<String> = arrayListOf(
            "-y",
            "-f",
            "concat",
            "-safe",
            "0",
            "-i",
            imageTextPath
        )
        if (audioPath != null) {
            cmd.add("-i")
            cmd.add(audioPath)
        }
        cmd.add("-preset")
        cmd.add("ultrafast")
        cmd.add("-vsync")
        cmd.add("vfr")
        cmd.add("-vf")
        cmd.add("scale=$videoWidth:$videoHeight:force_original_aspect_ratio=decrease,pad=$videoWidth:$videoHeight:(ow-iw)/2:(oh-ih)/2,format=yuv420p")
        cmd.add("-shortest")
        cmd.add(outputPath)
        return cmd.toArray(arrayOfNulls<String>(cmd.size))
    }

    suspend fun executeMergeVideo(
        mediaFiles: List<MediaFile>,
        isRemoveAudio: Boolean? = false,
        secondPerVideo: Int? = null,
        audioPath: String? = null,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?,
        scale: String? = ScaleType.SCALE_426_240.scale
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputVideoPath("merge_video")
            executeCommand(
                mergeVideos(
                    mediaFiles,
                    isRemoveAudio,
                    secondPerVideo,
                    outputPath,
                    scale,
                    audioPath
                ), onSuccess = { onSuccess?.invoke(outputPath) }, {
                    val fileImageVideo = File(outputPath)
                    if (fileImageVideo.exists()) fileImageVideo.delete()
                    onFail?.invoke(it)
                }
            )
        }
    }

    fun mergeVideos(
        mediaFiles: List<MediaFile>,
        isRemoveAudio: Boolean? = false,
        secondPerVideo: Int? = null,
        outputPath: String,
        scale: String? = null,
        audioPath: String?
    ): Array<String> {

        val inputs: ArrayList<String> = ArrayList()
        var query: String? = ""
        var queryAudio: String? = ""
        var index = 0
        mediaFiles.forEach { mediaFile ->
            if (mediaFile.mediaType == MediaFile.MEDIA_TYPE_VIDEO) {
                inputs.add("-i")
                inputs.add(mediaFile.path ?: "")

                query += "[" + index + ":v]" +
                        "scale=$scale:force_original_aspect_ratio=decrease," +
                        "pad=$scale:(ow-iw)/2:(oh-ih)/2,setsar=1,fps=24"
                if (secondPerVideo != null)
                    query += ",trim=0:$secondPerVideo"

                query += "[v$index];"
                queryAudio += "[v$index]"
                index++
            }
        }
        if (audioPath != null) {
            inputs.add("-i")
            inputs.add(audioPath)
        }
        inputs.apply {
            add("-y")
            add("-f")
            add("lavfi")
            add("-t")
            add("0.1")
            add("-i")
            add("anullsrc")
            add("-filter_complex")
            add("$query $queryAudio concat=n=$index:v=1:a=0${if (audioPath != null) "[vid]" else ""}")
            if (audioPath != null) {
                if (isRemoveAudio == false) {
                    add("-map")
                    add("[vid]")
                    add("-map")
                    add("[a]")
                } else {
                    add("-map")
                    add("[vid]")
                    add("-map")
                    add("${index}:a")
                    add("-c:a")
                    add("aac")
                    add("-strict")
                    add("-2")
                    add("-shortest")
                }
            } else if (isRemoveAudio == true) {
                add("-an")
            }
            add("-preset")
            add("ultrafast")
            add(outputPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    private fun getPathImageTextFile(mediaFiles: List<MediaFile>, duration: Int): String {
        val imageTextFile = File(tempDir, "images.txt")
        if (!imageTextFile.exists()) {
            try {
                imageTextFile.createNewFile()
            } catch (e: IOException) {
                Log.d(TAG, "Create image file error: ${e.printStackTrace()}")
            }
        }
        val sb = StringBuilder()
        mediaFiles.forEachIndexed { index, mediaFile ->
            sb.append("file '" + mediaFile.path.toString() + "'")
            sb.append("\n")
            sb.append("duration $duration")
            sb.append("\n")
            if (index == mediaFiles.size - 1) {
                sb.append("file '" + mediaFile.path.toString() + "'")
            }
        }
        try {
            val bufferedWriter = BufferedWriter(FileWriter(imageTextFile))
            bufferedWriter.append(sb)
            bufferedWriter.flush()
            bufferedWriter.close()
        } catch (ex: IOException) {
            Log.d(TAG, "Write image file error: ${ex.printStackTrace()}")
        }
        return imageTextFile.absolutePath
    }

    suspend fun executeMergeVideo1(
        videos: ArrayList<MediaFile>,
        secondsPerVideo: Int? = null,
        scale: String? = ScaleType.SCALE_426_240.scale,
        onSuccess: ((outputPath: String) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputVideoPath("merge_video")
            val cmdMergeVideo = cmdMergeVideosWithNewAudio(
                videos,
                scale,
                outputPath = outputPath,
                secondPerVideo = secondsPerVideo,
            )

            executeCommand(cmdMergeVideo, onSuccess = {
                onSuccess?.invoke(outputPath)
            }, onFail = {
                val fileImageVideo = File(outputPath)
                if (fileImageVideo.exists()) fileImageVideo.delete()
                onFail?.invoke(it)
            })
        }
    }

    suspend fun mergeAudioFile(
        audios: ArrayList<MediaFile>, onSuccess: ((resultPath: String) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputAudioPath("merge_audio")
            val complexCommand = arrayListOf(
                "-y",
            )

            audios.forEach { audio ->
                complexCommand.add("-i")
                complexCommand.add(audio.path ?: "")
            }

            complexCommand.add("-filter_complex")
//            complexCommand.add("amerge=inputs=${audios.size}:channel_layout=stereo")
            complexCommand.add("concat=n=${audios.size}:v=0:a=1")
            complexCommand.add("-c:a")
            complexCommand.add("mp3")
            complexCommand.add("-vn")
            complexCommand.add("-preset")
            complexCommand.add("ultrafast")
            complexCommand.add(outputPath)

            executeCommand(complexCommand.toArray(arrayOfNulls<String>(complexCommand.size)), {
                onSuccess?.invoke(outputPath)
            }, {
                val fileImageVideo = File(outputPath)
                if (fileImageVideo.exists()) fileImageVideo.delete()
                onFail?.invoke(it)
            })
        }
    }

    suspend fun executeImagesToVideo(
        images: ArrayList<MediaFile>,
        secondPerImage: Int,
        audioPath: String? = null,
        onSuccess: ((resultPath: String) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        withContext(Dispatchers.IO) {

            val outputPath = getOutputVideoPath("merge_image")
            val textImagesPath = getPathImageTextFile(images, secondPerImage)
            val cmd = cmdImagesToVideo(textImagesPath, outputPath, audioPath = audioPath)
            executeCommand(cmd, onSuccess = {
                // Delete temp image text file
                val fileImageText = File(textImagesPath)
                if (fileImageText.exists()) fileImageText.delete()

                onSuccess?.invoke(outputPath)
            }, onFail = {
                val fileImageVideo = File(outputPath)
                if (fileImageVideo.exists()) fileImageVideo.delete()
                onFail?.invoke(it)
            })
        }
    }


    fun cmdMergeVideosWithNewAudio(
        mediaFiles: List<MediaFile>,
        scale: String?,
        outputPath: String,
        secondPerVideo: Int?,
        audioPath: String? = null,
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        var query: String? = ""
        var queryAudio: String? = ""
        var index = 0

        mediaFiles.forEach { mediaFile ->
            if (mediaFile.mediaType == MediaFile.MEDIA_TYPE_VIDEO) {
                inputs.add("-i")
                inputs.add(mediaFile.path ?: "")

                query += "[" + index + ":v]" +
                        "scale=$scale:force_original_aspect_ratio=decrease," +
                        "pad=$scale:-1:-1,setsar=1,"
                if (secondPerVideo != null)
                    query += "trim=0:$secondPerVideo,"

                val endTime =
                    secondPerVideo?.minus(0.5) ?: mediaFile.duration?.toSecond()?.minus(0.5)
                query += "fps=24,fade=t=in:st=0:d=1,fade=t=out:st=$endTime:d=1[v$index];"

                queryAudio += "[v$index]"
                index++
            }
        }

        // Add audio
        if (audioPath != null) {
            inputs.add("-i")
            inputs.add(audioPath)
        }
        inputs.apply {
            add("-y")
            add("-f")
            add("lavfi")
            add("-i")
            add("anullsrc")
            add("-filter_complex")
            add("$query $queryAudio concat=n=$index:v=1:a=0[vid]")
            if (audioPath != null) {
                add("-map")
                add("[vid]")
                add("-map")
                add("${index}:a")
                add("-c:a")
                add("aac")
                add("-strict")
                add("-2")
            }
//            add("-shortest")
            add("-preset")
            add("ultrafast")
            add(outputPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    suspend fun trimAudio(
        audioPath: String, startSec: Int, endSec: Int,
        onSuccess: ((resultPath: String) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputAudioPath("trim_audio")
            val inputs = arrayListOf<String>()
            inputs.apply {
                add("-ss")
                add("$startSec")
                add("-i")
                add(audioPath)
                add("-t")
                add("${endSec - startSec}")
                add("-preset")
                add("ultrafast")
                add(outputPath)
            }
            executeCommand(inputs.toArray(arrayOfNulls<String>(inputs.size)),
                {
                    onSuccess?.invoke(outputPath)
                }, {
                    File(outputPath).deleteOnExit()
                    onFail?.invoke(it)
                }
            )
        }
    }


    /* Execute command*/
    fun executeCommand(
        command: Array<String>,
        onSuccess: (() -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        try {
            FFmpegKit.executeWithArgumentsAsync(command,
                { session ->
                    Log.d(TAG, "FFmpeg process exited with state $session")
                    when {
                        ReturnCode.isSuccess(session.returnCode) -> {
                            Log.d(TAG, "onSuccess")
                            onSuccess?.invoke()
                        }
                        ReturnCode.isCancel(session.returnCode) -> {
                            Log.e(TAG, " executeCommand is cancel:${session.failStackTrace}")
                            onFail?.invoke(session.failStackTrace)
                        }
                        session.returnCode != ReturnCode(ReturnCode.CANCEL) ||
                                session.returnCode != ReturnCode(ReturnCode.SUCCESS) -> {
                            Log.e(TAG, " executeCommand fail: ${session.failStackTrace}")
                            val error =
                                if (session.failStackTrace != null) session.failStackTrace else "Some error occur!"
                            onFail?.invoke(error)
                        }
                        else -> {
                            Log.e(TAG, " executeCommand fail:${session.failStackTrace}")
                        }
                    }
                }, {
                    Log.d(TAG, " onProgress $it")
                }, {
                    Log.d(TAG, "onStatistics $it")
                })
        } catch (ex: Error) {
            Log.e(TAG, " executeCommand error:${ex.printStackTrace()}")
            onFail?.invoke(ex.message ?: "")
        } catch (ex: Exception) {
            Log.e(TAG, "execute error: ${ex.printStackTrace()}")
            onFail?.invoke(ex.message ?: "")
        }
    }

    /* Cancel all process of FFmpegKit*/
    fun cancel() {
        FFmpegKit.cancel()
    }

}

