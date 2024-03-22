package com.github.crayonxiaoxin.alarmclock.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.github.crayonxiaoxin.lib_common.utils._e
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


object FileUtil {

    fun fileFromContentUri(
        context: Context,
        contentUri: Uri,
        filenameSuffix: String = "",
        filenamePrefix: String = "temp_file"
    ): File {
        // Preparing Temp file name
        val fileExtension = getFileExtension(context, contentUri)
        // 防止并发上传时相互覆盖的问题，指定一个特定的名称
        val fileSuffix =
            if (filenameSuffix.isEmpty()) "${System.currentTimeMillis()}" else "_$filenameSuffix"
        val fileName =
            filenamePrefix + fileSuffix + if (fileExtension != null) ".$fileExtension" else ""

        // Creating Temp file
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }

            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tempFile
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        Log.e("TAG", "getFileExtension: $fileType, $uri")
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }

    /**
     * 根据Uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    fun getRealPathFromUri(context: Context, uri: Uri): String? {
        val sdkVersion = Build.VERSION.SDK_INT
        return if (sdkVersion >= 19) { // api >= 19
            getRealPathFromUriAboveApi19(context, uri)
        } else { // api < 19
            getRealPathFromUriBelowAPI19(context, uri)
        }
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private fun getRealPathFromUriBelowAPI19(context: Context, uri: Uri): String? {
        return getDataColumn(context, uri, null, null)
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private fun getRealPathFromUriAboveApi19(context: Context, uri: Uri): String? {
        var filePath: String? = null
        _e("TAG", "getRealPathFromUriAboveApi19: uri = ${uri.scheme}")
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            val documentId = DocumentsContract.getDocumentId(uri)
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                val tmp = documentId.split(":".toRegex()).toTypedArray()
                val type = tmp[0]
                val id = tmp[1]
                if (type == "video") {
                    val selection = MediaStore.Video.Media._ID + "=?"
                    val selectionArgs = arrayOf(id)
                    filePath = getDataColumn(
                        context,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        selection,
                        selectionArgs
                    )
                } else {
                    _e("TAG", "getRealPathFromUriAboveApi19: document image")
                    val selection = MediaStore.Images.Media._ID + "=?"
                    val selectionArgs = arrayOf(id)
                    filePath = getDataColumn(
                        context,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selection,
                        selectionArgs
                    )
                    _e("TAG", "getRealPathFromUriAboveApi19: document image $filePath")
                }
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                val matches = Regex("[0-9]+").find(documentId)?.value ?: "0"
                _e("TAG", "isDownloadsDocument: $documentId,${matches},${uri.path}")

                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(matches)
                )
                filePath = getDataColumn(context, contentUri, null, null)
            } else {
                _e("TAG", "getRealPathFromUriAboveApi19: else $documentId, $uri")
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // 如果是 content 类型的 Uri
            _e("TAG", "getRealPathFromUriAboveApi19: content")
            filePath = getDataColumn(context, uri, null, null)
        } else if ("file" == uri.scheme) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            _e("TAG", "getRealPathFromUriAboveApi19: file")
            filePath = uri.path
        } else {
            _e("TAG", "getRealPathFromUriAboveApi19: else")
        }
        _e("TAG", "filepath = $filePath")
        return filePath
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                path = cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            cursor?.close()
        }
        return path
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun getFileName(context: Context, uri: Uri?): String {
        if (uri == null) return ""
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            cursor?.close()
        }
        return ""
    }
}

/**
 * 保存 jpeg 並返回 Uri，Android Q 以下的會返回 filePath
 */
fun Context.saveMediaToStorageUri(filename: String = "IMG_${System.currentTimeMillis()}.jpeg"): Pair<String, Uri?> {
    var currentFilePath = ""
    //Output stream
    var imageUri: Uri? = null
    //For devices running android >= Q
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //getting the contentResolver
        contentResolver?.also { resolver ->
            //Content resolver will process the contentvalues
            val contentValues = ContentValues().apply {
                //putting file information in content values
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            //Inserting the contentValues to contentResolver and getting the Uri
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        }
    } else {
        //These for devices running on android < Q
        //So I don't think an explanation is needed here
        val imagesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        currentFilePath = image.absolutePath
        imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Need to config provider in Manifest
            FileProvider.getUriForFile(this, "$packageName.provider", image)
        } else {
            Uri.fromFile(image)
        }
    }
    return Pair(currentFilePath, imageUri)
}

fun Context.getJpgFile(filename: String): File {
    return File(
        getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "${filename}.jpg"
    )
}

fun Context.getAudioFile(filename: String, ext: String = "mp3"): File {
    return File(
        getExternalFilesDir(Environment.DIRECTORY_MUSIC),
        "${filename}.${ext}"
    )
}

fun Context.getMp4File(filename: String): File {
    return File(
        getExternalFilesDir(Environment.DIRECTORY_MOVIES),
        "${filename}.mp4"
    )
}

fun Context?.createJpgUri(filename: String, ok: (Uri) -> Unit = {}) {
    if (this == null) return
    try {
        val jpgPath = getJpgFile(filename)
        if (!jpgPath.exists()) {
            jpgPath.createNewFile()
        }
        val fileUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            jpgPath
        )
        ok.invoke(fileUri)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context?.createAudioUri(filename: String, ext: String = "mp3", ok: (Uri) -> Unit = {}): Uri? {
    if (this == null) return null
    try {
        val mp4Path = getAudioFile(filename, ext)
        if (!mp4Path.exists()) {
            mp4Path.createNewFile()
        }
        val fileUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            mp4Path
        )
        ok.invoke(fileUri)
        return fileUri
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun Context?.createMp4Uri(filename: String, ok: (Uri) -> Unit = {}) {
    if (this == null) return
    try {
        val mp4Path = getMp4File(filename)
        if (!mp4Path.exists()) {
            mp4Path.createNewFile()
        }
        val fileUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            mp4Path
        )
        ok.invoke(fileUri)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context?.getExternalFiles(type: String = Environment.DIRECTORY_PICTURES): Array<out File> {
    this?.getExternalFilesDir(type)?.let {
        _e("File Utils", "$it")
        if (it.exists() && it.isDirectory) {
            return it.listFiles().orEmpty()
        }
    }
    return emptyArray()
}

fun Context?.clearExternalJpegFiles() {
    this.getExternalFiles(Environment.DIRECTORY_PICTURES).forEach {
        if (it.exists() && it.isFile) {
            _e("File Utils item", "$it")
            it.delete()
        }
    }
}

fun Context?.clearExternalMp4Files() {
    this.getExternalFiles(Environment.DIRECTORY_MOVIES).forEach {
        if (it.exists() && it.isFile) {
            _e("File Utils item", "$it")
            it.delete()
        }
    }
}

fun Context?.clearExternalFiles() {
    this.clearExternalJpegFiles()
    this.clearExternalMp4Files()
}

fun Context?.sizeExternalFiles(): Long {
    val sumOf = this.getExternalFiles(Environment.DIRECTORY_PICTURES).sumOf {
        it.length()
    }
    val sumOf1 = this.getExternalFiles(Environment.DIRECTORY_MOVIES).sumOf {
        it.length()
    }
    return sumOf + sumOf1
}

fun Long.spaceFmt(): String {
    val KB = 1024
    val MB = 1024 * KB
    val GB = 1024 * MB
    if (this >= GB) {
        val gb = this / GB * 1.0
        return "$gb G"
    } else if (this >= MB) {
        val mb = this / MB * 1.0
        return "$mb M"
    } else if (this > KB) {
        val kb = this / KB * 1.0
        return "$kb K"
    } else {
        return "$this B"
    }
}

fun Context?.getFileNameFromUri(uri: Uri): String {
    if (this == null) return ""
    var name = FileUtil.getFileName(this, uri)
    name = if (name.isEmpty()) {
        Uri.decode(uri.toString()).split("/").last()
    } else {
        name.split("/").last()
    }
    return name
}

fun Context.copyFile(source: Uri?): Uri? {
    if (source == null) return null
    var filename = getFileNameFromUri(source)
    val ext = filename.split(".").last()
    filename = filename.replace(".$ext", "")
    val sourceFile = FileUtil.fileFromContentUri(this, source, filenamePrefix = filename)
    val destUri = createAudioUri(filename, ext) ?: return null
    val destination = getAudioFile(filename, ext)
    if (!destination.exists()) {
        destination.createNewFile()
    }
    try {
        val sourceChannel = FileInputStream(sourceFile).channel
        val destinationChannel = FileOutputStream(destination).channel
        destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
        sourceChannel.close()
        destinationChannel.close()
        return destUri
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}