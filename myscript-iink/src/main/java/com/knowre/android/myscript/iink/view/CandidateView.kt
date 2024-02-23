package com.knowre.android.myscript.iink.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.knowre.android.myscript.iink.databinding.ViewJiixBinding
import com.knowre.android.myscript.iink.jiix.Item
import com.knowre.android.myscript.iink.jiix.Jiix
import com.knowre.android.myscript.iink.jiix.changeItem
import com.knowre.android.myscript.iink.jiix.changeLabel
import com.knowre.android.myscript.iink.jiix.findAllCollidingItems
import com.knowre.android.myscript.iink.jiix.firstItemOf
import com.knowre.android.myscript.iink.jiix.getCandidates
import com.knowre.android.myscript.iink.jiix.isValid
import com.knowre.android.myscript.iink.jiix.transformToRect
import com.knowre.android.myscript.iink.view.candidate.Candidate
import com.knowre.android.myscript.iink.view.candidate.CandidateAdapter


class CandidateView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    interface OnSymbolSelected {
        fun onSelected(jiix: Jiix)
    }

    var listener: OnSymbolSelected? = null

    lateinit var jiix: Jiix

    private val binding = ViewJiixBinding.inflate(LayoutInflater.from(context), this, true)

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
                .let { item -> listener?.onSelected(jiix.changeItem(item)) }
        },
        onExitClicked = {}
    )

    private var touchDownX: Float? = null
    private var touchDownY: Float? = null

    init {
        with(binding.candidate) {
            adapter = candidateAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
        }
    }

    private fun clear() {
        selectedRectF = null
        candidateAdapter.setCandidates(listOf())
    }

    private fun Item.drawBoundingBox() =
        also { selectedRectF = boundingBox.transformToRect(context) }

    private fun Item.bindCandidates() {
        jiix.getCandidates(this)
            .let { candidates ->
                candidateAdapter
                    .setCandidates(candidates.map { Candidate.Data(this.id, it) })

                if (candidates.isEmpty())
                    showNoCandidateAvailable()
            }
    }

    private fun List<Item>.nextSelectedItem() =
        currentlySelectedItem()
            ?.let { this[nextIndexOrZero(it)] }
            ?: run { this[0] }

    private fun List<Item>.currentlySelectedItem() =
        find { it.boundingBox.transformToRect(context) == selectedRectF }

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
}