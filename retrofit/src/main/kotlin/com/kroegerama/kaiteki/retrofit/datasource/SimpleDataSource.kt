package com.kroegerama.kaiteki.retrofit.datasource

import androidx.fragment.app.Fragment
import com.kroegerama.kaiteki.prepareLaunchWithProgress
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull

@Deprecated("use core SimpleDataSource instead")
data class SimpleDataSource<T>(
    val flow: SharedFlow<T>,
    val loading: SharedFlow<Boolean>,
    private val refreshFun: () -> Unit
) {

    fun refresh() = refreshFun()

    val latest get() = flow.replayCache.lastOrNull()

    companion object {

        @Deprecated("replace with core SimpleDataSource and custom progress handling")
        fun <T> Fragment.withSimpleDataSourceValue(
            dataSource: SimpleDataSource<T>,
            block: (T) -> Unit
        ) {
            val cached = dataSource.latest
            if (cached != null) {
                block(cached)
            } else {
                prepareLaunchWithProgress {
                    dataSource.flow.firstOrNull()
                }.startAndThen {
                    it?.let(block)
                }
            }
        }
    }
}
