package com.knowre.android.kal.myscript

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.knowre.android.kal.databinding.ViewMyscriptPadBinding
import com.knowre.android.myscript.iink.FolderProvider
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptAssetResource
import com.knowre.android.myscript.iink.MyScriptInitializer
import com.knowre.android.myscript.iink.MyScriptInterpretListener
import com.knowre.android.myscript.iink.ToolFunction
import com.knowre.android.myscript.iink.ToolType
import com.knowre.android.myscript.iink.certificate.MyCertificate
import com.knowre.android.myscript.iink.toByteArray
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

    private val myscript: MyScriptApi = MyScriptInitializer(
        context,
        MyCertificate.getBytes(),
        binding.myScript.editorView,
        FolderProvider(context),
        MyScriptAssetResource(context),
        mainScope
    )
        .setGeneralConfiguration()
        .setMathConfiguration()
        .writeAssetResourcesToMathResourceFolder()
        .initialize()

    init {
        binding.redo.isEnabled = false
        binding.undo.isEnabled = false

        binding.convert.setOnClickListener { myscript.convert() }
        binding.deleteAll.setOnClickListener { myscript.deleteAll() }
        myscript.listener = object : MyScriptInterpretListener {
            override fun onInterpreted(interpreted: String) {
                binding.latex.text = interpreted
                mainScope.launch {
                    binding.redo.isEnabled = myscript.canRedo
                    binding.undo.isEnabled = myscript.canUndo
                }
            }

            override fun onError(editor: Editor, blockId: String, error: EditorError, message: String) {
                Log.d("MY_SCRIPT_ERROR", "$error with message $message")
            }
        }

        binding.digitOnlyGrammar.setOnClickListener {
            myscript.loadMathGrammar("n_digit_exp", context.assets.toByteArray("n_digit_exp.res"))
        }

        binding.defaultGrammar.setOnClickListener {
            //TODO
        }

        binding.red.setOnClickListener {
            myscript.penColor = 0xFF0000
        }

        binding.blue.setOnClickListener {
            myscript.penColor = 0x0000FF
        }

        binding.black.setOnClickListener {
            myscript.penColor = 0x000000
        }

        binding.penSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.eraserSwitch.isChecked = false
            myscript.tool = if (isChecked) {
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
            myscript.isAutoConvertEnabled = isChecked
        }

        binding.eraserSwitch.setOnCheckedChangeListener { _, isChecked ->
            val toolType = if (binding.penSwitch.isChecked) ToolType.PEN else ToolType.HAND
            if (isChecked) {
                myscript.tool = MyScriptApi.Tool(toolType, ToolFunction.ERASING)
            } else {
                myscript.tool = MyScriptApi.Tool(toolType, ToolFunction.DRAWING)
            }
        }

        binding.redo.setOnClickListener {
            myscript.redo()
        }

        binding.undo.setOnClickListener {
            myscript.undo()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mainScope.cancel()
    }

}