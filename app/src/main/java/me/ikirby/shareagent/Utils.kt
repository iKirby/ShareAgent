package me.ikirby.shareagent

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

fun writeFile(contentResolver: ContentResolver, fileName: String, dir: File, relativePath: String, uri: Uri,
             runAfter: (String, String?) -> Unit) {
    val fileExtension = if (fileName.contains(".")) ".${fileName.substringAfterLast(".")}" else ""
    val fileNameWithoutExtension = fileName.substringBeforeLast(".")
    var file = File(dir, relativePath + fileName)
    var num = 0
    while (file.exists()) {
        file = File(dir, "$relativePath$fileNameWithoutExtension (${++num})$fileExtension")
    }
    if (!file.parentFile.exists()) {
        file.parentFile.mkdirs()
    }
    val inputStream = contentResolver.openInputStream(uri)
    val outputStream = file.outputStream()
    val buffer = ByteArray(1024)
    var length: Int
    while (true) {
        length = inputStream!!.read(buffer)
        if (length > 0) {
            outputStream.write(buffer, 0, length)
        } else {
            break
        }
    }
    inputStream.close()
    outputStream.close()
    runAfter.invoke(file.absolutePath, getMimeType(contentResolver, uri))
}

private fun getMimeType(contentResolver: ContentResolver, uri: Uri): String? {
    return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        contentResolver.getType(uri)
    } else {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase())
    }
}