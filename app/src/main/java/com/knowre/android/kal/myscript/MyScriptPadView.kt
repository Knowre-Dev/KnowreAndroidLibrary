package com.knowre.android.kal.myscript

import android.content.Context
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.knowre.android.kal.databinding.ViewMyscriptPadBinding
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptInitializer
import com.knowre.android.myscript.iink.MyScriptInterpretListener
import com.knowre.android.myscript.iink.ToolFunction
import com.knowre.android.myscript.iink.ToolType
import com.myscript.iink.Editor
import com.myscript.iink.EditorError
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

    private val candidateAdapter = CandidateAdapter(
        onCandidateClicked = { candidate -> },
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
        mainScope.launch {
            myScript = MyScriptInitializer(
                certificate = byteArrayOf(),
                myScriptView = binding.myScriptView,
                context = context,
                scope = mainScope
            )
                .initialize()
                .apply { addListener(interpretListener) }
                .apply { isAutoConvertEnabled = false }
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

        binding.candidateSwitch.setOnCheckedChangeListener { _, isChecked -> }
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