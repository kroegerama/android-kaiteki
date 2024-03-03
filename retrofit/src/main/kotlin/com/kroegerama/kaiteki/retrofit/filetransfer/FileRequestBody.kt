package com.kroegerama.kaiteki.retrofit.filetransfer

import android.annotation.SuppressLint
import android.webkit.MimeTypeMap
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File

class FileRequestBody(
    private val file: File,
    private val mediaType: MediaType? = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)?.toMediaTypeOrNull()
) : RequestBody() {

    override fun contentLength(): Long = file.length().takeUnless { it == 0L } ?: -1

    override fun contentType(): MediaType? = mediaType

    @SuppressLint("Recycle")
    override fun writeTo(sink: BufferedSink) {
        file.inputStream().source().use {
            sink.writeAll(it)
        }
    }
}
