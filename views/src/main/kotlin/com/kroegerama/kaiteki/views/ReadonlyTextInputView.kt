package com.kroegerama.kaiteki.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.kroegerama.kaiteki.provideDelegate

private val defStyleRes = com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu

class ReadonlyTextInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputOutlinedExposedDropdownMenuStyle
) : TextInputLayout(
    MaterialThemeOverlay.wrap(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ),
    attrs,
    defStyleAttr
) {

    var iconEmptyText: Drawable? = null
    var iconFilledText: Drawable? = null

    init {
        val wrapped = MaterialThemeOverlay.wrap(
            context, attrs, defStyleAttr, defStyleRes
        )
        wrapped.withStyledAttributes(
            attrs,
            R.styleable.ReadonlyTextInputView,
            defStyleAttr
        ) {
            iconEmptyText =
                getDrawable(R.styleable.ReadonlyTextInputView_iconEmptyText) ?: ContextCompat.getDrawable(context, R.drawable.ic_chevron_right)
            iconFilledText =
                getDrawable(R.styleable.ReadonlyTextInputView_iconFilledText) ?: ContextCompat.getDrawable(context, R.drawable.ic_clear)
        }

        val edit = MaterialAutoCompleteTextView(wrapped, attrs, androidx.appcompat.R.attr.autoCompleteTextViewStyle)
        addView(edit)
        endIconMode = END_ICON_CUSTOM
        endIconDrawable = if (edit.text.isNullOrEmpty()) iconEmptyText else iconFilledText

        descendantFocusability = FOCUS_BLOCK_DESCENDANTS
        isFocusable = false
        isFocusableInTouchMode = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isFocusedByDefault = false
        }

        setEndIconOnClickListener {
            clickListener?.onClick(this, true)
        }
        edit.setOnClickListener {
            clickListener?.onClick(this, false)
        }
        edit.inputType = EditorInfo.TYPE_NULL

        val colorControlHighlight = TypedValue().run {
            context.theme.resolveAttribute(androidx.appcompat.R.attr.colorControlHighlight, this, true)
            ContextCompat.getColor(context, resourceId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            edit.foreground = RippleDrawable(
                ColorStateList.valueOf(colorControlHighlight),
                null,
                edit.background
            )
        }
        edit.hint = null
        edit.doAfterTextChanged {
            val empty = it.isNullOrEmpty()
            endIconDrawable = if (empty) iconEmptyText else iconFilledText
        }

        isTransitionGroup = true
    }

    private var clickListener: OnReadOnlyClickListener? = null

    fun setOnClickListener(listener: OnReadOnlyClickListener?) {
        clickListener = listener
    }

    var text by editText!!

    fun interface OnReadOnlyClickListener {
        fun onClick(view: ReadonlyTextInputView, endIcon: Boolean)
    }
}
