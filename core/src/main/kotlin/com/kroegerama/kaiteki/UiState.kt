package com.kroegerama.kaiteki

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.kroegerama.kaiteki.flow.observeMultipleFlows
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

data class UiState<T>(
    val creator: () -> T,
    val isLoading: Boolean = false,
    val errors: List<UiError> = emptyList()
) {
    val state: T = creator()
}

data class UiError(
    val error: CharSequence,
    val id: Long = System.currentTimeMillis()
)

inline fun <T> MutableStateFlow<UiState<T>>.updateUiState(crossinline block: UiStateContext<T>.() -> Unit) {
    update { currentValue ->
        UiStateContext(currentValue).apply(block).build()
    }
}

class UiStateContext<T>(
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
    internal fun build() = UiState({ state }, isLoading, errors)
}

fun <T> Fragment.consumeState(
    state: Flow<UiState<T>>,
    updateUi: suspend (T) -> Unit,
    loading: suspend (Boolean) -> Unit,
    consumeError: (UiError) -> Unit,
    errorSnackBarDecorator: Snackbar.() -> Unit = {}
) {
    observeMultipleFlows {
        observe(state.map { it.state }.distinctUntilChanged()) { state ->
            updateUi(state)
        }
        observe(state.map { it.isLoading }.distinctUntilChanged()) { loading ->
            loading(loading)
        }
        observe(state.mapNotNull { it.errors.firstOrNull() }.distinctUntilChanged()) { error ->
            snackBar(error.error) {
                errorSnackBarDecorator()
                doOnDismiss {
                    consumeError(error)
                }
            }
        }
    }
}
