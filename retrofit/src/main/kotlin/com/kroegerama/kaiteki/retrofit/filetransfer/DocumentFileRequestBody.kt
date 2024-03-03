package com.kroegerama.kaiteki.retrofit.filetransfer

import android.annotation.SuppressLint
import android.content.Context
import androidx.documentfile.provider.DocumentFile
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

class DocumentFileRequestBody(
    private val context: Context,
    private val documentFile: DocumentFile,
    private val mediaType: MediaType? = documentFile.type?.toMediaTypeOrNull()
) : RequestBody() {

    override fun contentLength(): Long = documentFile.length().takeUnless { it == 0L } ?: -1

    override fun contentType(): MediaType? = mediaType

    @SuppressLint("Recycle")
    override fun writeTo(sink: BufferedSink) {
        val inputStream = context.contentResolver.openInputStream(documentFile.uri) ?: return
        inputStream.source().use {
            sink.writeAll(it)
        }
    }
}
