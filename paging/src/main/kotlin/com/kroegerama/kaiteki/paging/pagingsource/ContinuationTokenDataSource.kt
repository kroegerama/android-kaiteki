package com.kroegerama.kaiteki.paging.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kroegerama.kaiteki.retrofit.arrow.HttpError
import com.kroegerama.kaiteki.retrofit.arrow.IOError
import com.kroegerama.kaiteki.retrofit.arrow.UnexpectedError
import com.kroegerama.kaiteki.retrofit.arrow.loadResultError
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

abstract class ContinuationTokenDataSource<Token : Any, R : Any, T : Any> : PagingSource<Token, T>() {

    override fun getRefreshKey(state: PagingState<Token, T>): Token? = null

    override suspend fun load(params: LoadParams<Token>): LoadResult<Token, T> = try {
        val token = params.key
        val size = params.loadSize

        val response = makeCall(token, size)

        if (!response.isSuccessful) {
            HttpError(
                code = response.code(),
                message = response.message(),
                body = response.errorBody()
            ).loadResultError()
        } else {
            val responseBody = response.body()!!
            val data = responseBody.extractData()

            LoadResult.Page(
                data = postProcessData(responseBody, data),
                prevKey = null,
                nextKey = responseBody.extractNextToken()
            )
        }
    } catch (e: Exception) {
        if (e is CancellationException) {
            throw e
        }
        when (e) {
            is IOException -> IOError(e)
            else -> UnexpectedError(e)
        }.loadResultError()
    }

    abstract suspend fun makeCall(token: Token?, size: Int): Response<out R>
    abstract suspend fun R.extractData(): List<T>
    abstract suspend fun R.extractNextToken(): Token?
    open suspend fun postProcessData(responseBody: R?, data: List<T>): List<T> = data

    companion object {
        operator fun <Token : Any, T : Any> invoke(
            call: suspend (token: Token?, size: Int) -> Response<out Pair<Token?, List<T>>>
        ) = object : ContinuationTokenDataSource<Token, Pair<Token?, List<T>>, T>() {
            override suspend fun makeCall(token: Token?, size: Int): Response<out Pair<Token?, List<T>>> = call(token, size)
            override suspend fun Pair<Token?, List<T>>.extractData(): List<T> = second
            override suspend fun Pair<Token?, List<T>>.extractNextToken(): Token? = first
        }
    }
}
