package com.knowre.android.myscript.iink.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.knowre.android.myscript.iink.jiix.BoundingBox
import com.knowre.android.myscript.iink.jiix.Expression
import com.knowre.android.myscript.iink.jiix.Item
import com.knowre.android.myscript.iink.jiix.Jiix


class CandidateView constructor(
    context: Context,
    attrs: AttributeSet? = null

) : ConstraintLayout(context, attrs) {

    private var rectFs: List<RectF> = listOf()
        set(value) {
            field = value
            invalidate()
        }

    private var paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, android.R.color.holo_green_dark, null)
        style = Paint.Style.FILL
        alpha = 60
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        rectFs.forEach {
            canvas?.drawRect(it, paint)
        }
    }

    fun drawBoundingBoxes(jiix: Jiix) {
        rectFs = jiix
            .getAllItems()
            .map { transformToRect(it.boundingBox) }

        invalidate()
    }

    private fun Jiix.getAllItems(): List<Item> {
        return items + expressions.flatMap { it.getAllItems() }
    }

    private fun Expression.getAllItems(): List<Item> {
        return items + operands.flatMap { it.getAllItems() }
    }

    private fun transformToRect(box: BoundingBox) = RectF(
        mmToPx(context, box.x - 1F),
        mmToPx(context, box.y - 1F),
        mmToPx(context, box.x + box.width + 1F),
        mmToPx(context, box.y + box.height + 1F),
    )

    private fun mmToPx(context: Context, mm: Float): Float {
        val metrics = context.resources.displayMetrics
        return mm / 25.4f * metrics.xdpi
    }

    private fun mmToDp(context: Context, mm: Float): Float {
        val metrics = context.resources.displayMetrics
        return mm / 25.4f * (metrics.xdpi / 160f)
    }

}