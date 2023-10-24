package com.knowre.android.kal.myscript

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.knowre.android.kal.databinding.ViewMyscriptPadBinding
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptInterpretListener
import com.knowre.android.myscript.iink.ToolFunction
import com.knowre.android.myscript.iink.ToolType
import com.knowre.android.myscript.iink.copyAssetFileTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


internal class MyScriptPadView constructor(
    context: Context,
    attrs: AttributeSet? = null

) : FrameLayout(context, attrs) {

    private val binding = ViewMyscriptPadBinding.inflate(LayoutInflater.from(context), this, true)

    private val api: MyScriptApi = binding.myScript.api()

    init {
        binding.redo.isEnabled = false
        binding.undo.isEnabled = false

        binding.convert.setOnClickListener { binding.myScript.api().convert() }
        binding.deleteAll.setOnClickListener { binding.myScript.api().deleteAll() }
        binding.myScript.api().setInterpretListener(object : MyScriptInterpretListener {
            override fun onInterpreted(interpreted: String) {
                val canRedo = api.canRedo()
                val canUndo = api.canUndo()

                binding.latex.text = interpreted
                CoroutineScope(Dispatchers.Main).launch {
                    binding.redo.isEnabled = canRedo
                    binding.undo.isEnabled = canUndo
                }
                Log.d("MY_LOG", "onInterpreted $interpreted, $canRedo, $canUndo")
            }

            override fun onError(message: String) {
                Log.d("MY_LOG", "onError $message")
            }
        })

        binding.digitOnlyGrammar.setOnClickListener {
            val file = File(context.filesDir, "n_digit_exp.res").apply {
                context.copyAssetFileTo(assetFileName = "n_digit_exp.res", outputFile = this)!!
            }

            api.setGrammar(file)
        }

        binding.defaultGrammar.setOnClickListener {
            api.setGrammar(null)
        }

        binding.red.setOnClickListener {
            api.setPenColor(0xFF0000)
        }

        binding.blue.setOnClickListener {
            api.setPenColor(0x0000FF)
        }

        binding.black.setOnClickListener {
            api.setPenColor(0x000000)
        }

        binding.penSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.eraserSwitch.isChecked = false
            api.setPointerTool(if (isChecked) ToolType.PEN else ToolType.HAND, ToolFunction.DRAWING)
        }

        binding.eraserSwitch.setOnCheckedChangeListener { _, isChecked ->
            val toolType = if (binding.penSwitch.isChecked) ToolType.PEN else ToolType.HAND
            if (isChecked) {
                api.setPointerTool(toolType, ToolFunction.ERASING)
            } else {
                api.setPointerTool(toolType, ToolFunction.DRAWING)
            }
        }

        binding.redo.setOnClickListener {
            api.redo()
        }

        binding.undo.setOnClickListener {
            api.undo()
        }
    }

}