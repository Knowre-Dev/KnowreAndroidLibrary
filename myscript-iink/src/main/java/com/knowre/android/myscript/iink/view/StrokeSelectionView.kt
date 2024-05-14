package com.knowre.android.myscript.iink.view

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.knowre.android.extension.android.doOnPostLayout
import com.knowre.android.myscript.iink.databinding.ViewStorkeSelectionBinding
import com.knowre.android.myscript.iink.jiix.Item
import com.knowre.android.myscript.iink.jiix.Jiix
import com.knowre.android.myscript.iink.jiix.changeItem
import com.knowre.android.myscript.iink.jiix.changeLabel
import com.knowre.android.myscript.iink.jiix.findAllCollidingItems
import com.knowre.android.myscript.iink.jiix.firstItemOf
import com.knowre.android.myscript.iink.jiix.getCandidates
import com.knowre.android.myscript.iink.jiix.isValid
import com.knowre.android.myscript.iink.jiix.transformToRectF


class StrokeSelectionView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    interface Listener {
        fun onJiixChanged(jiix: Jiix)
        fun onViewHidden()
    }

    var listener: Listener? = null

    private lateinit var jiix: Jiix

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

    private val candidateAdapter = CandidateAdapter(
        onCandidateClicked = { candidate ->
            jiix.firstItemOf(candidate.id)
                .changeLabel(candidate.string)
                .let { item -> listener?.onJiixChanged(jiix.changeItem(item)) }
        },
        onExitClicked = {}
    )

    private var touchDownX: Float? = null
    private var touchDownY: Float? = null

    init {
        with(binding.candidate) {
            adapter = candidateAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            background = MaterialShapeDrawable(
                ShapeAppearanceModel.Builder()
                    .setAllCornerSizes(8F.dp)
                    .build()
            ).apply {
                tintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                strokeWidth = 1F.dp
                strokeColor = ColorStateList.valueOf(Color.parseColor("#CFD8DC"))
                elevation = 4F.dp
            }
        }
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
                touchDownX = event.x
                touchDownY = event.y
            }

            MotionEvent.ACTION_MOVE -> Unit

            MotionEvent.ACTION_UP -> {
                jiix.findAllCollidingItems(context, touchDownX!!, touchDownY!!)
                    .doOnNotEmpty {
                        nextSelectedItem()
                            .drawBoundingBox()
                            .also { item -> item.bindCandidates() }

                    }
            }

            else -> clear()
        }

        return true
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility != View.VISIBLE) {
            clear()
            listener?.onViewHidden()
        }
    }

    fun show(jiix: Jiix) {
        this.jiix = jiix
        isVisible = true
    }

    private fun clear() {
        selectedRectF = null
        candidateAdapter.setCandidates(listOf())
    }

    private fun Item.drawBoundingBox() =
        also { selectedRectF = boundingBox.transformToRectF(context) }

    private fun Item.bindCandidates() {
        jiix.getCandidates(this)
            .let { candidates ->
                if (candidates.isNotEmpty()) {
                    candidateAdapter
                        .setCandidates(candidates.map { Candidate.Data(this.id, it) })

                    binding.candidate.moveToTopOf(
                        rectF = boundingBox.transformToRectF(context),
                        margin = 10F.dp
                    )

                    binding.candidate.bringToFront()
                } else {
                    showNoCandidateAvailable()
                }
            }
    }

    private fun RecyclerView.moveToTopOf(rectF: RectF, margin: Float) {
        doOnPostLayout {
            val newX = (rectF.centerX() - (width / 2))
            val newY =  rectF.top - 10F.dp - height
            val rightLimit = this@StrokeSelectionView.width - width - margin
            updateLayoutParams {
                x = newX
                    .coerceAtLeast(margin)
                    .coerceAtMost(rightLimit)
                y = newY
            }
        }
    }

    private fun List<Item>.nextSelectedItem() =
        currentlySelectedItem()
            ?.let { this[nextIndexOrZero(it)] }
            ?: run { this[0] }

    private fun List<Item>.currentlySelectedItem() =
        find { it.boundingBox.transformToRectF(context) == selectedRectF }

    private fun List<Item>.doOnNotEmpty(action: List<Item>.() -> Unit) {
        if (isNotEmpty()) action(this)
    }

    private fun List<Item>.nextIndexOrZero(item: Item): Int {
        val index = this.indexOf(item)
        return if (index == -1 || index + 1 >= this.size) 0 else index + 1
    }

    private fun showNoCandidateAvailable() {
        Toast
            .makeText(context, "No candidates available.", Toast.LENGTH_SHORT)
            .show()
    }

    private val Number.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(),
            Resources.getSystem().displayMetrics
        )
}