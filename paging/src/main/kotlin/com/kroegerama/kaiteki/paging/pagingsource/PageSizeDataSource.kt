package com.kroegerama.kaiteki.paging.pagingsource

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.kroegerama.kaiteki.flow.UpdatableFlow
import com.kroegerama.kaiteki.flow.updatable
import com.kroegerama.kaiteki.retrofit.arrow.HttpError
import com.kroegerama.kaiteki.retrofit.arrow.IOError
import com.kroegerama.kaiteki.retrofit.arrow.UnexpectedError
import com.kroegerama.kaiteki.retrofit.arrow.loadResultError
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

abstract class PageSizeDataSource<R : Any, T : Any> : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> = try {
        val page = params.key ?: 0
        val size = params.loadSize

        val response = makeCall(page, size)

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
                prevKey = page.minus(1).takeUnless { it < 0 },
                nextKey = page.plus(1).takeUnless { data.size < size }
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

    abstract suspend fun makeCall(page: Int, size: Int): Response<out R>

    abstract suspend fun R.extractData(): List<T>

    open suspend fun postProcessData(responseBody: R?, data: List<T>): List<T> = data

    companion object {
        operator fun <T : Any> invoke(
            call: suspend (page: Int, size: Int) -> Response<out List<T>>
        ) = object : PageSizeDataSource<List<T>, T>() {
            override suspend fun makeCall(page: Int, size: Int): Response<out List<T>> = call(page, size)
            override suspend fun List<T>.extractData(): List<T> = this
        }
    }
}

fun <T : Any> ViewModel.pageSizePager(
    pageSize: Int = 20,
    call: suspend (page: Int, size: Int) -> Response<out List<T>>
): UpdatableFlow<PagingData<T>> {
    val source = PageSizeDataSource(call)
    val pager = Pager(
        config = PagingConfig(pageSize = pageSize, initialLoadSize = pageSize)
    ) {
        source
    }
    return pager.flow.cachedIn(viewModelScope).updatable {
        source.invalidate()
    }
}
