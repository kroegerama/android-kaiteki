package com.kroegerama.kaiteki.paging.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import arrow.core.Either
import arrow.core.getOrElse

abstract class ItemKeyedPagingSource<A, B, T : Any> : PagingSource<ItemKeyedPagingSource.DirectedItemKey<T>, T>() {

    private val knownIds = mutableSetOf<Any>()

    protected abstract suspend fun makePreviousCall(item: T?, size: Int): Either<A, B>?

    protected abstract suspend fun makeNextCall(item: T?, size: Int): Either<A, B>?

    protected abstract suspend fun B.data(): List<T>

    protected open suspend fun T.id(): Any? = null

    protected open suspend fun A.throwable(): Throwable = RuntimeException(toString())

    /**
     * attention: returning anything other than `null` can cause infinite loops,
     * because `load` will return `LoadResult.Invalid` on error, if `key != null`
     */
    override fun getRefreshKey(state: PagingState<DirectedItemKey<T>, T>): DirectedItemKey<T>? = null

    override suspend fun load(params: LoadParams<DirectedItemKey<T>>): LoadResult<DirectedItemKey<T>, T> {
        val key = params.key
        val size = params.loadSize

        val response = when (key) {
            null -> makeNextCall(null, size) ?: makePreviousCall(null, size)
            is DirectedItemKey.Previous -> makePreviousCall(key.key, size)
            is DirectedItemKey.Next -> makeNextCall(key.key, size)
        }?.getOrElse {
            return if (key == null) {
                LoadResult.Error(it.throwable())
            } else {
                LoadResult.Invalid()
            }
        }

        val data = response?.data().orEmpty()

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
            prevKey = when (key) {
                null,
                is DirectedItemKey.Previous -> data.takeUnless {
                    endReached
                }?.firstOrNull()?.let {
                    DirectedItemKey.Previous(it)
                }

                is DirectedItemKey.Next -> null
            },
            nextKey = when (key) {
                null,
                is DirectedItemKey.Next -> data.takeUnless {
                    endReached
                }?.lastOrNull()?.let {
                    DirectedItemKey.Next(it)
                }

                is DirectedItemKey.Previous -> null
            }
        )
    }

    sealed interface DirectedItemKey<out Key : Any> {
        val key: Key

        data class Previous<Key : Any>(
            override val key: Key
        ) : DirectedItemKey<Key>

        data class Next<Key : Any>(
            override val key: Key
        ) : DirectedItemKey<Key>
    }
}
