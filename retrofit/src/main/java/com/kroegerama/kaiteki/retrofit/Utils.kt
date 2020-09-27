package com.kroegerama.kaiteki.retrofit

import android.content.Context
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest

private val sha1 by lazy {
    MessageDigest.getInstance("SHA-1")
}

fun String.toSha1Hash(): String {
    val bytes = sha1.digest(toByteArray())
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        result.append(it.toString(16).padStart(2, '0'))
    }

    return result.toString()
}

fun <T, R> Call<T>.map(mapFunc: (T?) -> R?): Call<R> {
    return object : Call<R> {
        val self = this@map
        val me = this

        override fun enqueue(callback: Callback<R>) {
            self.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    callback.onFailure(me, t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    callback.onResponse(me, response.map(mapFunc))
                }
            })
        }

        override fun isExecuted() = self.isExecuted

        override fun clone(): Call<R> = self.clone().map(mapFunc)

        override fun isCanceled() = self.isCanceled

        override fun cancel() = self.cancel()

        override fun execute(): Response<R> = self.execute().map(mapFunc)

        override fun request() = self.request()

        override fun timeout() = self.timeout()
    }
}

fun <T, R> Response<T>.map(mapFunc: (T?) -> R?): Response<R> = if (isSuccessful) {
    Response.success(code(), mapFunc(body()))
} else {
    Response.error(errorBody()!!, raw())
}

val RetrofitResponse<*>.debugMessageHtml
    get() = with(this) {
        buildString {
            when (this@with) {
                is RetrofitResponse.NoSuccess -> {
                    val method = rawResponse.request().method()
                    val url = Html.escapeHtml(rawResponse.request().url().toString())
                    val code = code
                    val msg = Html.escapeHtml(errorBody?.string())
                    append("<br /><b>$method</b> ").append(url)
                    append("<br /><b>HTTP Status</b> ").append(code)
                    msg?.let { append("<br /><b>Message</b><br />").append(msg) }
                }
                is RetrofitResponse.Error -> {
                    val msg = Html.escapeHtml(exception.localizedMessage)
                    append("<br /><b>Exception</b><br />").append(msg)
                }
                is RetrofitResponse.Success -> append("Success")
                RetrofitResponse.Cancelled -> append("Cancelled")
            }
        }
    }

fun RetrofitResponse<*>.debugAlert(
    context: Context,
    titleHtml: String = "Debug<br/><small>(This message will only show in debug builds)</small>"
) {
    val msg = when (this@debugAlert) {
        is RetrofitResponse.NoSuccess,
        is RetrofitResponse.Error -> debugMessageHtml
        is RetrofitResponse.Success,
        RetrofitResponse.Cancelled -> return
    }

    AlertDialog.Builder(context)
        .setTitle(HtmlCompat.fromHtml(titleHtml, HtmlCompat.FROM_HTML_MODE_COMPACT))
        .setMessage(HtmlCompat.fromHtml(msg, HtmlCompat.FROM_HTML_MODE_COMPACT))
        .setPositiveButton(android.R.string.ok, null)
        .show()
}