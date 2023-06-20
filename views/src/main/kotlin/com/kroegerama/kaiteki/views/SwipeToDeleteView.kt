package com.kroegerama.kaiteki.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.withStyledAttributes
import com.kroegerama.kaiteki.provideDelegate
import com.kroegerama.kaiteki.views.databinding.ViewSwipeDeleteBinding

class SwipeToDeleteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), MotionLayout.TransitionListener {

    private val binding = ViewSwipeDeleteBinding.inflate(LayoutInflater.from(context), this, true)

    var swipeFinishedListener: SwipeFinishedListener? = null

    var swipeToDeleteText by binding.tvSwipeToDelete
    var deleteActionText by binding.tvDeleteAction
    var swipeThumbBackground
        get() = binding.btnSwipe.background
        set(value) {
            binding.btnSwipe.background = value
        }
    var swipeThumbIcon by binding.btnSwipe
    var swipeThumbIconTint
        get() = binding.btnSwipe.imageTintList?.defaultColor
        set(value) {
            binding.btnSwipe.imageTintList = value?.let(ColorStateList::valueOf)
        }
    var labelTextColor
        get() = binding.tvSwipeToDelete.currentTextColor
        set(value) {
            binding.tvSwipeToDelete.setTextColor(value)
            binding.tvDeleteAction.setTextColor(value)
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.SwipeToDeleteView, defStyleAttr) {
            swipeToDeleteText = getText(R.styleable.SwipeToDeleteView_swipeToDeleteText)
            deleteActionText = getText(R.styleable.SwipeToDeleteView_deleteActionText)
            swipeThumbBackground = getDrawable(R.styleable.SwipeToDeleteView_swipeThumbBackground)
            swipeThumbIcon = getDrawable(R.styleable.SwipeToDeleteView_swipeThumbIcon)
            swipeThumbIconTint = if (hasValue(R.styleable.SwipeToDeleteView_swipeThumbIconTint)) {
                getColor(R.styleable.SwipeToDeleteView_swipeThumbIconTint, 0)
            } else {
                null
            }
            labelTextColor = getColor(R.styleable.SwipeToDeleteView_labelTextColor, labelTextColor)
        }
        binding.swipeToDelete.addTransitionListener(this)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.root.enableTransition(R.id.transitionSwipeDelete, enabled)
    }

    override fun isEnabled(): Boolean {
        return super.isEnabled() && binding.root.getTransition(R.id.transitionSwipeDelete).isEnabled
    }

    fun reset() {
        binding.swipeToDelete.apply {
            getTransition(R.id.transitionSwipeDelete).isEnabled = true
            transitionToStart()
        }
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
        if (currentId == R.id.swipeDeleteEnd) {
            motionLayout.getTransition(R.id.transitionSwipeDelete).isEnabled = false
            swipeFinishedListener?.onSwipeFinished()
        }
    }

    override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {
        parent.requestDisallowInterceptTouchEvent(true)
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout,
        startId: Int,
        endId: Int,
        progress: Float
    ) = Unit

    override fun onTransitionTrigger(
        motionLayout: MotionLayout,
        triggerId: Int,
        positive: Boolean,
        progress: Float
    ) = Unit

    fun interface SwipeFinishedListener {
        fun onSwipeFinished()
    }
}
