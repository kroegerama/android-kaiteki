package com.kroegerama.kaiteki

import android.graphics.Rect
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.annotation.StringRes
import androidx.core.widget.NestedScrollView
import com.google.android.material.textfield.TextInputLayout

inline fun validate(block: ValidatorContext.() -> Unit): ValidatorContext = ValidatorContext().apply(block)

class ValidatorContext {
    var errorCount = 0
        private set

    @PublishedApi
    internal val errorViews = mutableListOf<View>()

    fun verify(relatedView: View? = null, check: () -> Boolean) {
        if (!check()) {
            relatedView?.let(errorViews::add)
            errorCount++
        }
    }

    fun verify(inputLayout: TextInputLayout, @StringRes errorTextRes: Int, check: () -> Boolean) =
        verify(inputLayout, inputLayout.context.getText(errorTextRes), check)

    fun verify(inputLayout: TextInputLayout, errorText: CharSequence, check: () -> Boolean) {
        val valid = check()
        inputLayout.error = if (valid) null else errorText
        if (!valid) {
            errorViews += inputLayout
            errorCount++
        }
    }

    inline fun onError(block: ValidatorResultContext.() -> Unit) {
        if (errorCount > 0) ValidatorResultContext(errorCount, errorViews).block()
    }

    data class ValidatorResultContext(
        val errorCount: Int,
        val errorTextInputLayouts: List<View>
    ) {
        fun scrollToFirstError(parentScrollView: ViewGroup) {
            val top = errorTextInputLayouts.map { view ->
                Rect().apply {
                    view.getDrawingRect(this)
                    parentScrollView.offsetDescendantRectToMyCoords(view, this)
                }
            }.minOfOrNull {
                it.top
            } ?: return
            when (parentScrollView) {
                is NestedScrollView -> parentScrollView.smoothScrollTo(0, top)
                is ScrollView -> parentScrollView.smoothScrollTo(0, top)
                else -> parentScrollView.scrollTo(0, top)
            }
        }
    }
}

val String.isValidEmail get() = Patterns.EMAIL_ADDRESS.matcher(this).matches()
val String.isValidWebsite get() = Patterns.WEB_URL.matcher(this).matches()
val String.isValidPhone get() = Patterns.PHONE.matcher(this).matches()
