package com.knowre.android.kal.myscript

import android.content.Context
import android.content.res.AssetManager
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import com.knowre.android.kal.databinding.ViewMyscriptPadBinding
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptInitializer
import com.knowre.android.myscript.iink.MyScriptInterpretListener
import com.knowre.android.myscript.iink.ToolFunction
import com.knowre.android.myscript.iink.ToolType
import com.knowre.android.myscript.iink.jiix.Jiix
import com.knowre.android.myscript.iink.view.StrokeSelectionBasicControl
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
    private lateinit var selectionControl: StrokeSelectionBasicControl

    init {
        initializeMyScript()
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
                .apply {
                    addListener(
                        object : MyScriptInterpretListener {
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
                    )
                    isAutoConvertEnabled = false
                }

            selectionControl = myScript.connectStrokeSelection(
                view = binding.strokeSelection,
                listener = object : StrokeSelectionView.Listener {
                    override fun onJiixChanged(jiix: Jiix) = Unit

                    override fun onViewHidden() {
                        binding.candidateSwitch.isChecked = false
                    }
                }
            )

            myScript
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
            selectionControl.enableStrokeSelectionMode(isChecked, onFailure = {
                when (it) {
                    MyScriptApi.StrokeSelectionModeError.EDITOR_BUSY -> Toast
                        .makeText(context, "Editor is busy", Toast.LENGTH_SHORT)
                        .show()

                    MyScriptApi.StrokeSelectionModeError.INVALID_STROKE -> Toast
                        .makeText(context, "Invalid Stroke", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }

    private fun AssetManager.toByteArray(fileName: String) = open(fileName).use { it.readBytes() }

}