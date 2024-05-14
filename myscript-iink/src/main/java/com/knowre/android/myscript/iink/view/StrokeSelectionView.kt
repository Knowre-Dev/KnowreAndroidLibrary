package com.knowre.android.myscript.iink.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.knowre.android.myscript.iink.databinding.ViewStorkeSelectionBinding
import com.knowre.android.myscript.iink.jiix.Item
import com.knowre.android.myscript.iink.jiix.Jiix
import com.knowre.android.myscript.iink.jiix.findAllCollidingItems
import com.knowre.android.myscript.iink.jiix.getCandidates
import com.knowre.android.myscript.iink.jiix.isValid
import com.knowre.android.myscript.iink.jiix.transformToRectF


class StrokeSelectionView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    interface Listener {
        fun onStrokeSelected(item: Item, candidates: List<String>)
        fun onNoStrokeSelected()
        fun onViewHidden()
    }

    var listener: Listener? = null

    lateinit var jiix: Jiix

    private val binding = ViewStorkeSelectionBinding.inflate(LayoutInflater.from(context), this, true)

    private var selectedRectF: RectF? = null
        set(value) {
            field = value
            invalidate()
        }

    private var selectedPaint = Paint().apply {
        color = ResourcesCompat.getColor(resources, android.R.color.holo_blue_dark, null)
        style = Paint.Style.FILL
        alpha = 60
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        selectedRectF?.let { canvas?.drawRect(it, selectedPaint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!::jiix.isInitialized || !jiix.isValid())
            return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                jiix.findAllCollidingItems(context, event.x,  event.y)
                    .doOn(
                        notEmpty = {
                            nextSelectedItem()
                                .drawBoundingBox()
                                .also { item ->
                                    listener?.onStrokeSelected(
                                        item = item,
                                        candidates = jiix.getCandidates(item)
                                    )
                                }
                        },
                        empty = {
                            listener?.onNoStrokeSelected()
                            clearRect()
                        }
                    )
            }
        }

        return true
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility != View.VISIBLE) {
            clearRect()
            listener?.onViewHidden()
        }
    }

    fun show(jiix: Jiix) {
        this.jiix = jiix
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

    private fun clearRect() {
        selectedRectF = null
        invalidate()
    }

    private fun Item.drawBoundingBox() =
        also { selectedRectF = boundingBox.transformToRectF(context) }

    private fun List<Item>.nextSelectedItem() =
        currentlySelectedItem()
            ?.let { this[nextIndexOrZero(it)] }
            ?: run { this[0] }

    private fun List<Item>.currentlySelectedItem() =
        find { it.boundingBox.transformToRectF(context) == selectedRectF }

    private fun List<Item>.doOn(
        notEmpty: List<Item>.() -> Unit,
        empty: List<Item>.() -> Unit
    ) {
        if (isNotEmpty()) notEmpty(this) else empty()
    }

    private fun List<Item>.nextIndexOrZero(item: Item): Int {
        val index = this.indexOf(item)
        return if (index == -1 || index + 1 >= this.size) 0 else index + 1
    }
}