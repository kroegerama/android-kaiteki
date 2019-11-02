package com.kroegerama.kaiteki.architecture

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

object LiveDataMerger {

    /**
     * Merge multiple LiveData sources into one MediatorLiveData.
     * @param merge function to merge the LiveData contents
     * @param waitForAll if set to **true**, the observer will only be notified,
     * if all source LiveData objects have emitted a value
     */
    fun <A, B, T> merge(
        sourceA: LiveData<A>, sourceB: LiveData<B>,
        waitForAll: Boolean = false,
        merge: (A?, B?) -> T?
    ): LiveData<T> {
        val mediator = MediatorLiveData<T>()

        var emittedA = !waitForAll
        var emittedB = !waitForAll

        val mergeInternal = { a: A?, b: B? ->
            if (emittedA && emittedB) {
                mediator.value = merge(a, b)
            }
        }

        mediator.addSource(sourceA) { a ->
            emittedA = true
            mergeInternal.invoke(a, sourceB.value)
        }
        mediator.addSource(sourceB) { b ->
            emittedB = true
            mergeInternal.invoke(sourceA.value, b)
        }

        return mediator
    }

    /**
     * Merge multiple LiveData sources into one MediatorLiveData.
     * @param merge function to merge the LiveData contents
     * @param waitForAll if set to **true**, the observer will only be notified,
     * if all source LiveData objects have emitted a value
     */
    fun <A, B, C, T> merge(
        sourceA: LiveData<A>, sourceB: LiveData<B>, sourceC: LiveData<C>,
        waitForAll: Boolean = false,
        merge: (A?, B?, C?) -> T?
    ): LiveData<T> {
        val mediator = MediatorLiveData<T>()

        var emittedA = !waitForAll
        var emittedB = !waitForAll
        var emittedC = !waitForAll

        val mergeInternal = { a: A?, b: B?, c: C? ->
            if (emittedA && emittedB && emittedC) {
                mediator.value = merge(a, b, c)
            }
        }

        mediator.addSource(sourceA) { a ->
            emittedA = true
            mergeInternal.invoke(a, sourceB.value, sourceC.value)
        }
        mediator.addSource(sourceB) { b ->
            emittedB = true
            mergeInternal.invoke(sourceA.value, b, sourceC.value)
        }
        mediator.addSource(sourceC) { c ->
            emittedC = true
            mergeInternal.invoke(sourceA.value, sourceB.value, c)
        }

        return mediator
    }

    fun <T, A, B> split(
        source: LiveData<T>,
        split: (T?) -> Pair<A?, B?>
    ): Pair<LiveData<A>, LiveData<B>> {
        val resultA: LiveData<A> = Transformations.map(source) { split(it).first }
        val resultB: LiveData<B> = Transformations.map(source) { split(it).second }
        return Pair(resultA, resultB)
    }

    fun <T, A, B, C> split(
        source: LiveData<T>,
        split: (T?) -> Triple<A?, B?, C?>
    ): Triple<LiveData<A>, LiveData<B>, LiveData<C>> {
        val resultA: LiveData<A> = Transformations.map(source) { split(it).first }
        val resultB: LiveData<B> = Transformations.map(source) { split(it).second }
        val resultC: LiveData<C> = Transformations.map(source) { split(it).third }
        return Triple(resultA, resultB, resultC)
    }
}