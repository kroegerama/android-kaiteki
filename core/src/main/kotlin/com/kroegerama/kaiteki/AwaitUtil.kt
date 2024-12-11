/*
 * See https://chris.banes.dev/suspending-views/
 * See https://chris.banes.dev/suspending-views-example/
 */
package com.kroegerama.kaiteki

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume


suspend fun View.awaitNextLayout() = suspendCancellableCoroutine<Unit> { cont ->
    val listener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            removeOnLayoutChangeListener(this)
            cont.resume(Unit)
        }
    }
    cont.invokeOnCancellation { removeOnLayoutChangeListener(listener) }
    addOnLayoutChangeListener(listener)
}

suspend fun View.awaitLayout() {
    if (isLaidOut && !isLayoutRequested) {
        return
    }
    awaitNextLayout()
}

suspend fun Animator.awaitEnd() = suspendCancellableCoroutine<Unit> { cont ->
    val listener = object : AnimatorListenerAdapter() {
        private var endedSuccessfully = true

        override fun onAnimationCancel(animation: Animator) {
            endedSuccessfully = false
        }

        override fun onAnimationEnd(animation: Animator) {
            removeListener(this)
            if (cont.isActive) {
                if (endedSuccessfully) {
                    cont.resume(Unit)
                } else {
                    cont.cancel()
                }
            }
        }
    }
    cont.invokeOnCancellation { cancel() }
    addListener(listener)
}

suspend fun MotionLayout.awaitTransitionComplete(transitionId: Int, timeout: Long = 5000L) {
    if (currentState == transitionId) return

    var listener: MotionLayout.TransitionListener? = null

    try {
        withTimeout(timeout) {
            suspendCancellableCoroutine<Unit> { cont ->
                object : TransitionAdapter() {
                    override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                        if (currentId == transitionId) {
                            removeTransitionListener(this)
                            cont.resume(Unit)
                        }
                    }
                }.apply {
                    cont.invokeOnCancellation { removeTransitionListener(this) }
                    addTransitionListener(this)
                    listener = this
                }
            }
        }
    } catch (e: TimeoutCancellationException) {
        listener?.let(::removeTransitionListener)
        throw CancellationException("Transition to state with id: $transitionId did not complete in timeout.", e)
    }
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.findItemIdPosition(itemId: Long): Int {
    return (0 until itemCount).firstOrNull { getItemId(it) == itemId } ?: RecyclerView.NO_POSITION
}

suspend fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.awaitItemIdExists(itemId: Long): Int {
    val currentPos = findItemIdPosition(itemId)
    if (currentPos != RecyclerView.NO_POSITION) return currentPos

    return suspendCancellableCoroutine { cont ->
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                (positionStart until positionStart + itemCount).forEach { position ->
                    if (getItemId(position) == itemId) {
                        unregisterAdapterDataObserver(this)
                        cont.resume(position)
                    }
                }
            }
        }
        cont.invokeOnCancellation { unregisterAdapterDataObserver(observer) }
        registerAdapterDataObserver(observer)
    }
}

suspend fun RecyclerView.awaitScrollEnd() {
    // If a smooth scroll has just been started, it won't actually start until the next
    // animation frame, so we'll await that first
    awaitAnimationFrame()
    // Now we can check if we're actually idle. If so, return now
    if (scrollState == RecyclerView.SCROLL_STATE_IDLE) return

    suspendCancellableCoroutine<Unit> { cont ->
        val listener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    removeOnScrollListener(this)
                    cont.resume(Unit)
                }
            }
        }

        cont.invokeOnCancellation { removeOnScrollListener(listener) }
        addOnScrollListener(listener)
    }
}

suspend fun View.awaitAnimationFrame() = suspendCancellableCoroutine<Unit> { cont ->
    val runnable = Runnable { cont.resume(Unit) }
    cont.invokeOnCancellation { removeCallbacks(runnable) }
    postOnAnimation(runnable)
}