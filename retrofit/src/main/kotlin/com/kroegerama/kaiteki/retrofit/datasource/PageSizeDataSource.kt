package com.kroegerama.kaiteki.retrofit.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import retrofit2.Response
import timber.log.Timber

abstract class PageSizeDataSource<R : Any, T : Any> : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> = try {
        val page = params.key ?: 0
        val size = params.loadSize

        val loadingEndMin = System.currentTimeMillis() + minLoadingTimeMs

        val response = makeCall(page, size)
        if (!response.isSuccessful) {
            LoadResult.Error(IllegalStateException("Response code was ${response.code()}"))
        } else {
            val responseBody = response.body()
            val data = responseBody?.extractData().orEmpty()
            val next = if (data.size < size) null else page + 1

            val timeDelta = loadingEndMin - System.currentTimeMillis()
            if (timeDelta > 10) {
                delay(timeDelta)
            }

            LoadResult.Page(
                data = postProcessData(responseBody, data),
                prevKey = null,
                nextKey = next
            )
        }
    } catch (e: Exception) {
        Timber.w(e)
        LoadResult.Error(e)
    }

    abstract suspend fun makeCall(page: Int, size: Int): Response<R>

    abstract suspend fun R.extractData(): List<T>

    open suspend fun postProcessData(responseBody: R?, data: List<T>): List<T> = data

    open val minLoadingTimeMs = 200
}
