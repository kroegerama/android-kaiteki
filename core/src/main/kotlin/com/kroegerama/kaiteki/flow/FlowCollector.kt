package com.kroegerama.kaiteki.flow

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class FlowCollectorDsl

/**
 * Safe collection of flows in [LifecycleOwner]s Uses [flowWithLifecycle] under the hood.
 *
 * Example:
 * ```
 * observeFlow(flow) { /* ... */ }
 * ```
 * @see flowWithLifecycle
 */
@FlowCollectorDsl
inline fun <reified T> Flow<T>.observeWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit
): Job = lifecycleOwner.lifecycleScope.launch {
    flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState).collect(action)
}

@FlowCollectorDsl
inline fun <reified T> Flow<T>.observeWithLifecycle(
    fragment: Fragment,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit
): Job = observeWithLifecycle(fragment.viewLifecycleOwner, minActiveState, action)

@FlowCollectorDsl
inline fun <reified T> LifecycleOwner.observeFlow(
    flow: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit
): Job = flow.observeWithLifecycle(this, minActiveState, action)

@FlowCollectorDsl
inline fun <reified T> Fragment.observeFlow(
    flow: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit
): Job = flow.observeWithLifecycle(viewLifecycleOwner, minActiveState, action)

@Suppress("DeprecatedCallableAddReplaceWith")
@JvmInline
@FlowCollectorDsl
value class MultipleFlowCollectorContext @PublishedApi internal constructor(
    @PublishedApi
    internal val scope: CoroutineScope
) {

    @Deprecated("Not allowed in this context", level = DeprecationLevel.ERROR)
    fun <T> Flow<T>.observeWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ): Job = error("Not allowed in this context")

    @Deprecated("Not allowed in this context", level = DeprecationLevel.ERROR)
    fun <T> Flow<T>.observeWithLifecycle(
        fragment: Fragment,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ): Job = error("Not allowed in this context")

    @Deprecated("Not allowed in this context", level = DeprecationLevel.ERROR)
    fun <T> LifecycleOwner.observeFlow(
        flow: Flow<T>,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ): Job = error("Not allowed in this context")

    @Deprecated("Not allowed in this context", level = DeprecationLevel.ERROR)
    fun <T> Fragment.observeFlow(
        flow: Flow<T>,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ): Job = error("Not allowed in this context")

    @Deprecated("Not allowed in this context", level = DeprecationLevel.ERROR)
    fun LifecycleOwner.observeMultipleFlows(
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        block: (@FlowCollectorDsl MultipleFlowCollectorContext).() -> Unit
    ): Job = error("Not allowed in this context")

    inline fun <reified T> observe(
        flow: Flow<T>,
        noinline action: suspend (T) -> Unit
    ) {
        scope.launch {
            flow.collect(action)
        }
    }
}

/**
 * Collect multiple flows at the same time. Uses [Lifecycle.repeatOnLifecycle] under the hood.
 *
 * Example:
 * ```
 * observeMultipleFlows {
 *     observe(flow1) { /* ... */ }
 *     observe(flow2) { /* ... */ }
 * }
 * ```
 * @see Lifecycle.repeatOnLifecycle
 */
@FlowCollectorDsl
inline fun LifecycleOwner.observeMultipleFlows(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: (@FlowCollectorDsl MultipleFlowCollectorContext).() -> Unit
): Job = lifecycleScope.launch {
    repeatOnLifecycle(minActiveState) {
        block(
            MultipleFlowCollectorContext(
                scope = this
            )
        )
    }
}

/**
 * @see LifecycleOwner.observeMultipleFlows
 */
@FlowCollectorDsl
inline fun Fragment.observeMultipleFlows(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: (@FlowCollectorDsl MultipleFlowCollectorContext).() -> Unit
): Job = viewLifecycleOwner.observeMultipleFlows(minActiveState, block)
