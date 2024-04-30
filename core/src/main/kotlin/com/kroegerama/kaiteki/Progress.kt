package com.kroegerama.kaiteki

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kroegerama.kaiteki.architecture.launchAndCollectWithLifecycleState
import com.kroegerama.kaiteki.databinding.ProgressBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Progress : DialogFragment() {

    init {
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(
        requireContext(),
        requireContext().resolveResourceIdAttribute(
            R.attr.progressDialogThemeOverlay
        ).takeUnless { it == 0 } ?: R.style.ThemeOverlay_Kaiteki_ProgressDialog
    ).setView(
        ProgressBinding.inflate(layoutInflater).apply {
            val title = arguments?.getCharSequence(ARG_TITLE, null)
            tvTitle.isVisible = title != null
            tvTitle.text = title
        }.root
    ).setCancelable(false).create().apply {
        window?.attributes?.alpha = .96f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dismissSignal.filter {
            it == tag
        }.launchAndCollectWithLifecycleState(this) {
            dismissAllowingStateLoss()
        }
    }

    companion object {
        const val ARG_TITLE = "arg.progress.modal.title"

        val dismissSignal = MutableSharedFlow<String>(replay = 2, extraBufferCapacity = 5)

        suspend operator fun <T> invoke(
            fragmentManager: FragmentManager,
            title: CharSequence? = null,
            block: suspend CoroutineScope.() -> T
        ): T = coroutineScope {
            val tag = UUID.randomUUID().toString()
            val dlg = Progress().apply {
                arguments = bundle {
                    putCharSequence(ARG_TITLE, title)
                }
            }
            val minDismiss = System.currentTimeMillis() + 300
            try {
                withContext(Dispatchers.Main.immediate) {
                    dlg.show(fragmentManager, tag)
                }
                block()
            } catch (e: Exception) {
                throw e
            } finally {
                withContext(NonCancellable) {
                    val delta = minDismiss - System.currentTimeMillis()
                    if (delta > 50) {
                        delay(delta)
                    }
                    dismissSignal.emit(tag)
                }
            }
        }
    }
}

@DslMarker
annotation class ProgressDSL

class ProgressContext<T> internal constructor(
    private val context: CoroutineContext,
    private val fragmentManager: FragmentManager,
    private val title: CharSequence? = null,
    private val checkAllowPost: () -> Boolean,
    private val block: suspend CoroutineScope.() -> T
) {
    @ProgressDSL
    fun start() = startAndThen {}

    @ProgressDSL
    fun startAndThen(post: (T) -> Unit) {
        ProcessLifecycleOwner.get().lifecycleScope.launch(
            context
        ) {
            val result = Progress(fragmentManager, title, block)
            withContext(Dispatchers.Main.immediate) {
                if (checkAllowPost()) {
                    post(result)
                }
            }
        }
    }
}

@ProgressDSL
fun <T> Fragment.prepareLaunchWithProgress(
    context: CoroutineContext = EmptyCoroutineContext,
    title: CharSequence? = null,
    block: suspend CoroutineScope.() -> T
): ProgressContext<T> = ProgressContext(
    context = context,
    fragmentManager = childFragmentManager,
    title = title,
    checkAllowPost = {
        val viewLifecycleState = viewLifecycleOwnerLiveData.value?.lifecycle?.currentState ?: Lifecycle.State.DESTROYED
        viewLifecycleState.isAtLeast(Lifecycle.State.CREATED)
    },
    block = block
).also {
    view?.closeKeyboard()
}

@ProgressDSL
fun <T> Fragment.launchWithProgress(
    context: CoroutineContext = EmptyCoroutineContext,
    title: CharSequence? = null,
    block: suspend CoroutineScope.() -> T
) = prepareLaunchWithProgress(context, title, block).start()

@ProgressDSL
fun <T> AppCompatActivity.prepareLaunchWithProgress(
    context: CoroutineContext = EmptyCoroutineContext,
    title: CharSequence? = null,
    block: suspend CoroutineScope.() -> T
): ProgressContext<T> = ProgressContext(
    context = context,
    fragmentManager = supportFragmentManager,
    title = title,
    checkAllowPost = { lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED) },
    block = block
).also {
    findViewById<View>(android.R.id.content)?.closeKeyboard()
}

@ProgressDSL
fun <T> AppCompatActivity.launchWithProgress(
    context: CoroutineContext = EmptyCoroutineContext,
    title: CharSequence? = null,
    block: suspend CoroutineScope.() -> T
) = prepareLaunchWithProgress(context, title, block).start()
