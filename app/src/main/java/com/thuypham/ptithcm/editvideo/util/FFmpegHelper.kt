package com.thuypham.ptithcm.editvideo.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.thuypham.ptithcm.editvideo.model.MediaFile
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
            }, onFail)
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
            }, onFail)
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
            }, onFail)
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
            }, onFail)
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
            }, onFail)
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

    private fun getDefaultAudioPath(): String {
        val audioPath = "${outputDir.absolutePath}/default_audio.mp3"
        try {
            val audioFile = File(audioPath)
            if (!audioFile.exists()) {
//                val audioRes = R.raw.default_audio
//                val inputStream = context.resources.openRawResource(audioRes)
//                inputStream.toFile(audioPath)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return audioPath
    }

    fun cmdMergeAudioWithVideo(
        videoPath: String,
        audioPath: String = getDefaultAudioPath(),
        outputPath: String
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-y")
            add("-i")
            add(videoPath)
            add("-i")
            add(audioPath)
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

    fun mergeVideos(
        mediaFiles: List<MediaFile>,
        videoWidth: Int? = DEFAULT_WIDTH,
        videoHeight: Int? = DEFAULT_HEIGHT,
        outputPath: String,
        secondPerVideo: Int? = DEFAULT_SECOND,
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
                        "scale=$videoWidth:$videoHeight:force_original_aspect_ratio=decrease,pad=$videoWidth:$videoHeight:(ow-iw)/2:(oh-ih)/2,"
//                        "scale=(iw*sar)*max($videoWidth/(iw*sar)\\,$videoHeight/ih):ih*max($videoWidth/(iw*sar)\\,$videoHeight/ih)," +
//                        "crop=$videoWidth:$videoHeight," +
                "trim=0:$secondPerVideo," +
                        "fps=24,setpts=PTS-STARTPTS[v$index];"

                queryAudio += "[v$index]"
                index++
            }
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
            add("$query $queryAudio concat=n=$index:v=1:a=0")
            add("-an")
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

    fun executeMergeVideo(
        videos: ArrayList<MediaFile>,
        onSuccess: ((MediaFile?) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
        secondsPerVideo: Int,
        outputPath: String,
        projectId: String,
        imagesVideoPath: String? = null
    ) {
        val cmdMergeVideo = cmdMergeVideosWithNewAudio(
            videos,
            outputPath = outputPath,
            secondPerVideo = secondsPerVideo,
            imagesVideoPath = imagesVideoPath
        )

        executeCommand(cmdMergeVideo, onSuccess = {

            // Get info of video created --> cast to MediaFile
            // Todo: Update: get media video info by path
            val currentMillis = System.currentTimeMillis()
            val mediaFile = MediaFile(
                id = currentMillis,
                path = outputPath,
                dateAdded = currentMillis,
                duration = 0,
                mediaType = MediaFile.MEDIA_TYPE_VIDEO,
                displayName = projectId,
            )
            onSuccess?.invoke(mediaFile)
        }, onFail = {
            val fileImageVideo = File(outputPath)
            if (fileImageVideo.exists()) fileImageVideo.delete()
            onFail?.invoke(it)
        })
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
        videoWidth: Int = DEFAULT_WIDTH,
        videoHeight: Int = DEFAULT_HEIGHT,
        outputPath: String,
        secondPerVideo: Int = DEFAULT_SECOND,
        audioPath: String = getDefaultAudioPath(),
        imagesVideoPath: String? = null,
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        var query: String? = ""
        var queryAudio: String? = ""
        var index = 0

        // Set video create by images at the first video merge
        imagesVideoPath?.let { imageVidPath ->
            inputs.add("-i")
            inputs.add(imageVidPath)
            query += "[" + index + ":v]" +
                    "scale=$videoWidth:$videoHeight:force_original_aspect_ratio=decrease," +
                    "pad=$videoWidth:$videoHeight:-1:-1,setsar=1," +
                    "fps=24," +
                    "fade=t=in:st=0:d=1,fade=t=out:st=${secondPerVideo.minus(0.5)}:d=1[v$index];"

            queryAudio += "[v$index]"
            index++
        }

        mediaFiles.forEach { mediaFile ->
            if (mediaFile.mediaType == MediaFile.MEDIA_TYPE_VIDEO) {
                inputs.add("-i")
                inputs.add(mediaFile.path ?: "")

                query += "[" + index + ":v]" +
                        "scale=$videoWidth:$videoHeight:force_original_aspect_ratio=decrease," +
                        "pad=$videoWidth:$videoHeight:-1:-1,setsar=1," +
                        "trim=0:$secondPerVideo," +
                        "fps=24," +
                        "fade=t=in:st=0:d=1,fade=t=out:st=${secondPerVideo.minus(0.5)}:d=1[v$index];"

                queryAudio += "[v$index]"
                index++
            }
        }

        // Add audio
        inputs.add("-i")
        inputs.add(audioPath)
        inputs.apply {
            add("-y")
            add("-f")
            add("lavfi")
            add("-t")
            add("0.1")
            add("-i")
            add("anullsrc")
            add("-filter_complex")
            add("$query $queryAudio concat=n=$index:v=1:a=0[vid]")
            add("-map")
            add("[vid]")
            add("-map")
            add("${index}:a")
            add("-c:a")
            add("aac")
            add("-strict")
            add("-2")
            add("-shortest")
            add("-preset")
            add("ultrafast")
            add(outputPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
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

