package me.ikirby.shareagent

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

fun writeFile(contentResolver: ContentResolver, fileName: String, dir: File, relativePath: String, uri: Uri,
              runAfter: (String?, String?) -> Unit) {
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
    if (inputStream == null) {
        runAfter.invoke(null, null)
        return
    }
    val outputStream = file.outputStream()
    val buffer = ByteArray(1024)
    var length: Int
    while (true) {
        length = inputStream.read(buffer)
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

fun removeParamsFromURL(urlWithParams: String, paramsToRemove: List<String>): String {
    var url = urlWithParams.substringBefore("?")
    val currentParams = urlWithParams.substringAfter("?").split("&").toList()

    val paramsMap = mutableMapOf<String, String>()
    currentParams.forEach {
        val arr = it.split("=")
        if (!paramsToRemove.contains(arr[0])) {
            paramsMap[arr[0]] = arr[1]
        }
    }
    if (paramsMap.isNotEmpty()) {
        var isFirst = true
        for (entry: Map.Entry<String, String> in paramsMap) {
            if (isFirst) {
                url = "$url?${entry.key}=${entry.value}"
                isFirst = false
            } else {
                url = "$url&${entry.key}=${entry.value}"
            }
        }
    }
    return url
}
