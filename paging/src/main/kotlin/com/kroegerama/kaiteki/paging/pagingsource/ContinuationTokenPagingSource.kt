package com.kroegerama.kaiteki.paging.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import arrow.core.Either
import arrow.core.getOrElse

abstract class ContinuationTokenPagingSource<A, B, Token : Any, T : Any> : PagingSource<Token, T>() {

    private val knownIds = mutableSetOf<Any>()

    protected abstract suspend fun makeCall(token: Token?, size: Int): Either<A, B>

    protected abstract suspend fun B.data(): List<T>

    protected abstract suspend fun B.continuationToken(): Token?

    protected open suspend fun T.id(): Any? = null

    protected open suspend fun A.throwable(): Throwable = RuntimeException(toString())

    /**
     * attention: returning anything other than `null` can cause infinite loops,
     * because `load` will return `LoadResult.Invalid` on error, if `token != null`
     */
    override fun getRefreshKey(state: PagingState<Token, T>): Token? = null

    override suspend fun load(params: LoadParams<Token>): LoadResult<Token, T> {
        val token = params.key
        val size = params.loadSize

        val response = makeCall(token, size).getOrElse {
            return if (token == null) {
                LoadResult.Error(it.throwable())
            } else {
                // token may be invalid -> try without any token
                LoadResult.Invalid()
            }
        }

        val data = response.data()
        val continuationToken = response.continuationToken()

        val isValid = data.all {
            val id = it.id() ?: return@all true
            knownIds.add(id)
        }
        if (!isValid) {
            return LoadResult.Invalid()
        }

        return LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = continuationToken
        )
    }
}
