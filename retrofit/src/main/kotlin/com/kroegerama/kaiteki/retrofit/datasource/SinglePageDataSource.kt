package com.kroegerama.kaiteki.retrofit.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kroegerama.kaiteki.retrofit.arrow.HttpError
import com.kroegerama.kaiteki.retrofit.arrow.IOError
import com.kroegerama.kaiteki.retrofit.arrow.UnexpectedError
import com.kroegerama.kaiteki.retrofit.arrow.loadResultError
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

abstract class SinglePageDataSource<R : Any, T : Any> : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> = try {
        val response = makeCall()

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
                nextKey = null
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

    abstract suspend fun makeCall(): Response<out R>
    abstract suspend fun R.extractData(): List<T>
    open suspend fun postProcessData(responseBody: R?, data: List<T>): List<T> = data
}
