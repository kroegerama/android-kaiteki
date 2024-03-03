package com.kroegerama.kaiteki.retrofit.filetransfer

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.kroegerama.kaiteki.retrofit.arrow.UnexpectedError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.buffer
import okio.sink
import java.io.File

abstract class FileNetworkControllerBase {

    abstract val client: OkHttpClient

    suspend fun downloadFile(sourceUrl: String, target: File): Either<UnexpectedError, Unit> = either {
        val request = Request.Builder()
            .url(sourceUrl)
            .get()
            .build()
        val call = client.newCall(request)
        val response = catch(
            block = {
                withContext(Dispatchers.IO) {
                    call.execute()
                }
            },
            catch = {
                raise(UnexpectedError(it))
            }
        )
        ensure(response.isSuccessful) { UnexpectedError(IllegalStateException("download error ${response.code}")) }
        val body = ensureNotNull(response.body) { UnexpectedError(IllegalStateException("empty response")) }
        catch(
            block = {
                target.sink().buffer().use { out ->
                    body.source().use { `in` ->
                        out.writeAll(`in`)
                    }
                }
            },
            catch = { raise(UnexpectedError(it)) }
        )
    }

    suspend fun uploadFile(
        file: File,
        targetUrl: String,
        method: String = "PUT"
    ) = uploadFile(
        targetUrl = targetUrl,
        method = method
    ) {
        FileRequestBody(file)
    }

    suspend fun uploadContentUri(
        context: Context,
        uri: Uri,
        targetUrl: String,
        method: String = "PUT"
    ) = either {
        val documentFile = ensureNotNull(DocumentFile.fromSingleUri(context, uri)) {
            UnexpectedError(IllegalStateException("cannot get DocumentFile"))
        }
        uploadDocumentFile(
            context = context,
            documentFile = documentFile,
            targetUrl = targetUrl,
            method = method
        ).bind()
    }

    suspend fun uploadDocumentFile(
        context: Context,
        documentFile: DocumentFile,
        targetUrl: String,
        method: String = "PUT"
    ) = uploadFile(
        targetUrl = targetUrl,
        method = method
    ) {
        DocumentFileRequestBody(
            context = context,
            documentFile = documentFile
        )
    }

    private suspend fun uploadFile(
        targetUrl: String,
        method: String,
        bodyBuilder: () -> RequestBody
    ) = either {
        val request = Request.Builder()
            .url(targetUrl)
            .method(method, bodyBuilder())
            .build()
        val call = client.newCall(request)
        val response = catch(
            block = {
                withContext(Dispatchers.IO) {
                    call.execute()
                }
            },
            catch = { raise(UnexpectedError(it)) }
        )
        ensure(response.isSuccessful) { UnexpectedError(IllegalStateException("upload error ${response.code}")) }
    }

}
