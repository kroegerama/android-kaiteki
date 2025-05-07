package com.kroegerama.kaiteki.paging.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import arrow.core.Either
import arrow.core.getOrElse

abstract class PageSizePagingSource<A, B, T : Any> : PagingSource<Int, T>() {

    private val knownIds = mutableSetOf<Any>()

    protected abstract suspend fun makeCall(page: Int, size: Int): Either<A, B>

    protected abstract suspend fun B.data(): List<T>

    protected open suspend fun T.id(): Any? = null

    protected open suspend fun A.throwable(): Throwable = RuntimeException(toString())

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        val key = page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
        return key?.coerceAtLeast(0)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 0
        val size = params.loadSize

        val response = makeCall(page, size).getOrElse {
            return LoadResult.Error(it.throwable())
        }

        val data = response.data()
        val isValid = data.all {
            val id = it.id() ?: return@all true
            knownIds.add(id)
        }
        if (!isValid) {
            return LoadResult.Invalid()
        }

        val endReached = data.size < size

        return LoadResult.Page(
            data = data,
            prevKey = page.minus(1).takeUnless { it < 0 },
            nextKey = page.plus(1).takeUnless { endReached }
        )
    }
}
