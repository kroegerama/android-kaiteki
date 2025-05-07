package com.kroegerama.kaiteki

import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.google.android.material.R
import com.google.android.material.progressindicator.BaseProgressIndicator
import java.lang.ref.WeakReference

fun ImageRequest.Builder.progress(progress: BaseProgressIndicator<*>) = listener(
    onStart = { progress.show() },
    onError = { _, _ -> progress.hide() },
    onCancel = { progress.hide() },
    onSuccess = { _, _ -> progress.hide() }
)

@RequiresApi(Build.VERSION_CODES.M)
fun ImageRequest.Builder.progress(view: View): ImageRequest.Builder {
    val progressDrawable = view.context.getIndeterminateProgressDrawable().apply {
        (this as? Animatable)?.start()

        setTint(view.context.getThemeColor(R.attr.colorPrimary))
    }
    val wrapper = GravityDrawable(progressDrawable, Color.TRANSPARENT.toDrawable())

    val weakView = WeakReference(view)

    fun show() {
        weakView.get()?.foreground = wrapper
        wrapper.setVisible(true, false)
    }

    fun hide() {
        weakView.get()?.foreground = null
        wrapper.setVisible(false, false)
    }

    return listener(
        onStart = { show() },
        onError = { _, _ -> hide() },
        onCancel = { hide() },
        onSuccess = { _, _ -> hide() }
    )
}

inline fun ImageView.loadWithProgress(
    data: Any?,
    imageLoader: ImageLoader = context.imageLoader,
    builder: ImageRequest.Builder.() -> Unit = {}
) = load(data, imageLoader) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        progress(this@loadWithProgress)
    }
    apply(builder)
}
