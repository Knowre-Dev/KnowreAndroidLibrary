package com.knowre.android.kal.myscript

import android.content.Context
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.knowre.android.extension.android.doOnPostLayout
import com.knowre.android.kal.databinding.ViewMyscriptPadBinding
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptInitializer
import com.knowre.android.myscript.iink.MyScriptInterpretListener
import com.knowre.android.myscript.iink.ToolFunction
import com.knowre.android.myscript.iink.ToolType
import com.knowre.android.myscript.iink.jiix.Item
import com.knowre.android.myscript.iink.jiix.transformToRectF
import com.knowre.android.myscript.iink.view.StrokeSelectionModeBasicControl
import com.knowre.android.myscript.iink.view.StrokeSelectionModeError
import com.knowre.android.myscript.iink.view.StrokeSelectionView
import com.myscript.iink.Editor
import com.myscript.iink.EditorError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


internal class MyScriptPadView constructor(
    context: Context,
    attrs: AttributeSet? = null

) : FrameLayout(context, attrs) {

    private val binding = ViewMyscriptPadBinding.inflate(LayoutInflater.from(context), this, true)

    private val mainScope = MainScope()

    private lateinit var myScript: MyScriptApi
    private lateinit var selectionControl: StrokeSelectionModeBasicControl

    private val candidateAdapter = CandidateAdapter(
        onCandidateClicked = { candidate ->
            selectionControl
                .changeLabel(candidate.itemId, candidate.label)
        },
        onExitClicked = {}
    )

    init {
        initializeMyScript()
        initializeRecyclerView()
        initializeToolsListener()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mainScope.cancel()
    }

    private fun initializeMyScript() {
        mainScope.launch(Dispatchers.Default) {
            myScript = MyScriptInitializer(
                myScriptView = binding.myScriptView,
                context = context,
                scope = mainScope
            )
                .initialize()
                .apply { addListener(interpretListener) }
                .apply { isAutoConvertEnabled = false }

            selectionControl = myScript.useBasicSelectionControl(
                view = binding.strokeSelection,
                listener = selectionListener
            )
        }
    }

    private fun initializeRecyclerView() {
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

    private fun initializeToolsListener() {
        binding.redo.isEnabled = false
        binding.undo.isEnabled = false

        binding.deleteAll.setOnClickListener { myScript.eraseAll() }
        binding.digitOnlyGrammar.setOnClickListener {
            myScript.loadMathGrammar("n_digit_exp", context.assets.toByteArray("n_digit_exp.res"))
        }

        binding.defaultGrammar.setOnClickListener {
            //TODO
        }

        binding.red.setOnClickListener {
            myScript.penColor = 0xFF0000
        }

        binding.blue.setOnClickListener {
            myScript.penColor = 0x0000FF
        }

        binding.black.setOnClickListener {
            myScript.penColor = 0x000000
        }

        binding.convert.setOnClickListener { myScript.convert() }

        binding.penSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.eraserSwitch.isChecked = false
            myScript.tool = if (isChecked) {
                MyScriptApi.Tool(
                    toolType = ToolType.PEN,
                    toolFunction = ToolFunction.DRAWING
                )
            } else {
                MyScriptApi.Tool(
                    toolType = ToolType.HAND,
                    toolFunction = ToolFunction.DRAWING
                )
            }
        }

        binding.convertSwitch.setOnCheckedChangeListener { _, isChecked ->
            myScript.isAutoConvertEnabled = isChecked
        }

        binding.eraserSwitch.setOnCheckedChangeListener { _, isChecked ->
            val toolType = if (binding.penSwitch.isChecked) ToolType.PEN else ToolType.HAND
            if (isChecked) {
                myScript.tool = MyScriptApi.Tool(toolType, ToolFunction.ERASING)
            } else {
                myScript.tool = MyScriptApi.Tool(toolType, ToolFunction.DRAWING)
            }
        }

        binding.redo.setOnClickListener {
            myScript.redo()
        }

        binding.undo.setOnClickListener {
            myScript.undo()
        }

        binding.candidateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectionControl.enable(onFailure = {
                    when (it) {
                        StrokeSelectionModeError.EDITOR_BUSY ->
                            Toast
                                .makeText(context, "Editor is busy", Toast.LENGTH_SHORT)
                                .show()

                        StrokeSelectionModeError.INVALID_STROKE ->
                            Toast
                                .makeText(context, "Invalid Stroke", Toast.LENGTH_SHORT)
                                .show()
                    }
                })
            } else {
                selectionControl.disable()
            }
        }
    }

    private fun RecyclerView.moveToTopOf(rectF: RectF, margin: Float) {
        doOnPostLayout {
            val newX = (rectF.centerX() - (width / 2))
            val newY = rectF.top - 10F.dp - height
            val rightLimit = this@MyScriptPadView.width - width - margin
            updateLayoutParams {
                x = newX
                    .coerceAtLeast(margin)
                    .coerceAtMost(rightLimit)
                y = newY
            }
        }
    }

    private val interpretListener: MyScriptInterpretListener
        get() = object : MyScriptInterpretListener {
            override fun onInterpreted(interpreted: String) {
                binding.latex.text = interpreted
                binding.redo.isEnabled = myScript.canRedo
                binding.undo.isEnabled = myScript.canUndo
            }

            override fun onInterpretError(editor: Editor, blockId: String, error: EditorError, message: String) {
                Log.d("MY_SCRIPT_ERROR", "$error with message $message")
            }

            override fun onImportError() {
                Toast
                    .makeText(context, "해당 문자로는 변경이 불가능합니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val selectionListener: StrokeSelectionView.Listener
        get() = object : StrokeSelectionView.Listener {
            override fun onStrokeSelected(item: Item, candidates: List<String>) {
                if (candidates.isNotEmpty()) {
                    candidateAdapter
                        .setCandidates(candidates.map { Candidate.Data(item.id, it) })

                    binding.candidate.moveToTopOf(
                        rectF = item.boundingBox.transformToRectF(context),
                        margin = 10F.dp
                    )
                } else {
                    showNoCandidateAvailable()
                }
            }

            override fun onNoStrokeSelected() {
                candidateAdapter.clear()
            }

            override fun onViewHidden() {
                binding.candidateSwitch.isChecked = false
                candidateAdapter.clear()
            }
        }

    private fun showNoCandidateAvailable() {
        Toast
            .makeText(context, "No candidates available.", Toast.LENGTH_SHORT)
            .show()
    }

    private fun AssetManager.toByteArray(fileName: String) = open(fileName).use { it.readBytes() }

    private val Number.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(),
            Resources.getSystem().displayMetrics
        )
}