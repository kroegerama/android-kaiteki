package com.kroegerama.kaiteki.paging

import androidx.lifecycle.LifecycleOwner
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import arrow.core.Either
import com.kroegerama.kaiteki.flow.observeMultipleFlows
import com.kroegerama.kaiteki.retrofit.arrow.TypedCallError
import com.kroegerama.kaiteki.retrofit.arrow.exception
import com.kroegerama.kaiteki.retrofit.datasource.SimpleDataSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

abstract class PagingControllerBase<
        EmptyConfig,
        NoDataAdapter,
        LoadStateAdapter,
        ErrorAdapter,
        ErrorResponseType> where NoDataAdapter : RecyclerView.Adapter<*>,
                                 NoDataAdapter : PagingControllerBase.NoData<EmptyConfig>,
                                 LoadStateAdapter : androidx.paging.LoadStateAdapter<*>,
                                 ErrorAdapter : androidx.paging.LoadStateAdapter<*> {

    fun RecyclerView.setupPagingAdapterWithRetryRefresh(
        lifecycleOwner: LifecycleOwner,
        pagingAdapter: PagingDataAdapter<*, *>,
        swipeRefreshLayout: SwipeRefreshLayout? = parent as? SwipeRefreshLayout,
        emptyConfigFlow: Flow<EmptyConfig> = flowOf(emptyConfig()),
        additionalHeaderAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>? = null,
        additionalFooterAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>? = null
    ) {
        val emptyAdapter = noDataAdapter()
        val headerAdapter = if (swipeRefreshLayout != null) {
            errorAdapter { pagingAdapter.refresh() }
        } else {
            loadStateAdapter { pagingAdapter.refresh() }
        }

        adapter = ConcatAdapter(
            listOfNotNull(
                additionalHeaderAdapter,
                emptyAdapter,
                headerAdapter,
                pagingAdapter.withLoadStateHeaderAndFooter(
                    header = loadStateAdapter { pagingAdapter.retry() },
                    footer = loadStateAdapter { pagingAdapter.retry() }
                ),
                additionalFooterAdapter
            )
        )
        pagingAdapter.addLoadStateListener {
            val refreshState = it.refresh
            swipeRefreshLayout?.isRefreshing = refreshState == LoadState.Loading
            headerAdapter.loadState = refreshState
        }

        swipeRefreshLayout?.setOnRefreshListener { pagingAdapter.refresh() }

        lifecycleOwner.observeMultipleFlows {
            observe(
                pagingAdapter.loadStateFlow.distinctUntilChangedBy {
                    it.refresh
                }.filter {
                    it.refresh is LoadState.NotLoading
                }
            ) {
                scrollToPosition(0)
            }

            observe(pagingAdapter.onPagesUpdatedFlow.conflate()) {
                emptyAdapter.visible = pagingAdapter.itemCount == 0
            }

            observe(emptyConfigFlow) { emptyConfig ->
                emptyAdapter.value = emptyConfig
            }
        }
    }

    fun RecyclerView.setupListAdapterWithRetryRefresh(
        lifecycleOwner: LifecycleOwner,
        listAdapter: ListAdapter<*, *>,
        errorFlow: Flow<TypedCallError<ErrorResponseType>?>,
        refreshingFlow: Flow<Boolean>,
        refreshFun: () -> Unit,
        swipeRefreshLayout: SwipeRefreshLayout? = parent as? SwipeRefreshLayout,
        emptyConfigFlow: Flow<EmptyConfig> = flowOf(emptyConfig()),
        additionalHeaderAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>? = null,
        additionalFooterAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>? = null
    ) {
        val emptyAdapter = noDataAdapter()
        val headerAdapter = if (swipeRefreshLayout != null) {
            errorAdapter(refreshFun)
        } else {
            loadStateAdapter(refreshFun)
        }

        val dataAdapters = ConcatAdapter(
            headerAdapter,
            listAdapter
        )
        adapter = ConcatAdapter(
            listOfNotNull(
                additionalHeaderAdapter,
                emptyAdapter,
                dataAdapters,
                additionalFooterAdapter
            )
        )

        swipeRefreshLayout?.setOnRefreshListener(refreshFun)

        val itemCountFlow = dataAdapters.itemCountFlow()
        lifecycleOwner.observeMultipleFlows {
            if (swipeRefreshLayout != null) {
                observe(refreshingFlow) { refreshing ->
                    swipeRefreshLayout.isRefreshing = refreshing
                }
            }
            observe(
                combine(
                    errorFlow,
                    refreshingFlow,
                    itemCountFlow
                ) { callError, refreshing, itemCount ->
                    when {
                        refreshing -> LoadState.Loading
                        callError != null -> LoadState.Error(callError.exception())
                        else -> LoadState.NotLoading(true)
                    } to itemCount
                }.conflate()
            ) { (state, itemCount) ->
                headerAdapter.loadState = state
                emptyAdapter.visible = itemCount == 0 && state !is LoadState.Loading
            }
            observe(emptyConfigFlow) { emptyConfig ->
                emptyAdapter.value = emptyConfig
            }
        }
    }

    fun <T> RecyclerView.setupListAdapterWithRetryRefresh(
        lifecycleOwner: LifecycleOwner,
        listAdapter: ListAdapter<*, *>,
        simpleDataSource: SimpleDataSource<Either<TypedCallError<ErrorResponseType>, T>>,
        swipeRefreshLayout: SwipeRefreshLayout? = parent as? SwipeRefreshLayout,
        emptyConfigFlow: Flow<EmptyConfig> = flowOf(emptyConfig()),
        additionalHeaderAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>? = null,
        additionalFooterAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>? = null
    ) {
        setupListAdapterWithRetryRefresh(
            lifecycleOwner = lifecycleOwner,
            listAdapter = listAdapter,
            errorFlow = simpleDataSource.flow.map { it.leftOrNull() }.onStart { emit(null) }.distinctUntilChanged(),
            refreshingFlow = simpleDataSource.loading.onStart { emit(true) }.distinctUntilChanged(),
            refreshFun = simpleDataSource::refresh,
            swipeRefreshLayout = swipeRefreshLayout,
            emptyConfigFlow = emptyConfigFlow,
            additionalHeaderAdapter = additionalHeaderAdapter,
            additionalFooterAdapter = additionalFooterAdapter
        )
    }

    private fun RecyclerView.Adapter<*>.itemCountFlow() = callbackFlow {
        send(itemCount)
        fun update() {
            trySend(itemCount)
        }

        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = update()
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = update()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = update()
        }
        registerAdapterDataObserver(observer)
        awaitClose {
            unregisterAdapterDataObserver(observer)
        }
    }.distinctUntilChanged()

    abstract fun emptyConfig(): EmptyConfig
    abstract fun noDataAdapter(): NoDataAdapter
    abstract fun loadStateAdapter(retryFun: () -> Unit): LoadStateAdapter
    abstract fun errorAdapter(retryFun: () -> Unit): ErrorAdapter

    interface NoData<EmptyConfig> {
        var visible: Boolean
        var value: EmptyConfig?
    }

}
