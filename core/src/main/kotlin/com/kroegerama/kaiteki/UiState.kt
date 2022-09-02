package com.kroegerama.kaiteki

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.*
import java.util.*

data class UiState<T>(
    val creator: () -> T,
    val isLoading: Boolean = false,
    val errors: List<UiError> = emptyList()
) {
    val state: T = creator()
}

data class UiError(
    val error: CharSequence,
    val id: Long = UUID.randomUUID().mostSignificantBits
)

inline fun <T> MutableStateFlow<UiState<T>>.updateUiState(block: UiStateContext<T>.() -> Unit) {
    update { currentValue ->
        UiStateContext(currentValue.creator, currentValue).apply(block).build()
    }
}

class UiStateContext<T>(
    private val creator: () -> T,
    uiState: UiState<T>
) {

    @PublishedApi
    internal var state: T = uiState.state
    var isLoading = uiState.isLoading
    private val errors = uiState.errors.toMutableList()

    inline fun updateData(block: (T) -> T) {
        state = block(state)
    }

    fun addError(error: CharSequence?) {
        error ?: return
        errors += UiError(error)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun consumeError(id: Long) {
        errors.removeIf { it.id == id }
    }

    fun consumeError(uiError: UiError) {
        errors.remove(uiError)
    }

    fun consumeAllErrors() {
        errors.clear()
    }

    @PublishedApi
    internal fun build() = UiState(creator, isLoading, errors)
}

fun <T> Fragment.consumeState(
    state: Flow<UiState<T>>,
    updateUi: suspend (T) -> Unit,
    loading: suspend (Boolean) -> Unit,
    consumeError: (UiError) -> Unit,
    errorSnackBarDecorator: Snackbar.() -> Unit = {}
) {
    launchWhenViewCreated {
        state.map { it.state }.distinctUntilChanged().flowWithLifecycle(viewLifecycleOwner.lifecycle).collectLatest { state ->
            updateUi(state)
        }
    }
    launchWhenViewCreated {
        state.map { it.isLoading }.distinctUntilChanged().flowWithLifecycle(viewLifecycleOwner.lifecycle).collectLatest { loading ->
            loading(loading)
        }
    }
    launchWhenViewCreated {
        state.mapNotNull { it.errors.firstOrNull() }.distinctUntilChanged().flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .collectLatest { error ->
                snackBar(error.error) {
                    errorSnackBarDecorator()
                    doOnDismiss {
                        consumeError(error)
                    }
                }
            }
    }
}
