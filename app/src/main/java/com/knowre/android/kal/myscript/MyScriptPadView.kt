package com.knowre.android.kal.myscript

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.knowre.android.kal.databinding.ViewMyscriptPadBinding


internal class MyScriptPadView constructor(
    context: Context,
    attrs: AttributeSet? = null

) : FrameLayout(context, attrs) {

    private val binding = ViewMyscriptPadBinding.inflate(LayoutInflater.from(context), this, true)

//    private val myscript: MyScriptApi = MyScriptBuilder().build(
//        context,
//        MyCertificate.getBytes(),
//        binding.myScript.editorView,
//        FolderProvider(context),
//        MyScriptAssetResource(context),
//        MainScope()
//    )
//
//    init {
//        binding.redo.isEnabled = false
//        binding.undo.isEnabled = false
//
//        binding.convert.setOnClickListener { myscript.convert() }
//        binding.deleteAll.setOnClickListener { myscript.deleteAll() }
//        myscript.setInterpretListener(object : MyScriptInterpretListener {
//            override fun onInterpreted(interpreted: String) {
//                val canRedo = myscript.canRedo()
//                val canUndo = myscript.canUndo()
//
//                binding.latex.text = interpreted
//                CoroutineScope(Dispatchers.Main).launch {
//                    binding.redo.isEnabled = canRedo
//                    binding.undo.isEnabled = canUndo
//                }
//            }
//
//            override fun onError(editorError: message: String) = Unit
//        })
//
//        binding.digitOnlyGrammar.setOnClickListener {
//            myscript.loadMathGrammar("n_digit_exp", context.assets.toByteArray("n_digit_exp.res"))
//        }
//
//        binding.defaultGrammar.setOnClickListener {
//            //TODO
//        }
//
//        binding.red.setOnClickListener {
//            myscript.setPenColor(0xFF0000)
//        }
//
//        binding.blue.setOnClickListener {
//            myscript.setPenColor(0x0000FF)
//        }
//
//        binding.black.setOnClickListener {
//            myscript.setPenColor(0x000000)
//        }
//
//        binding.penSwitch.setOnCheckedChangeListener { _, isChecked ->
//            binding.eraserSwitch.isChecked = false
//            myscript.setPointerTool(if (isChecked) ToolType.PEN else ToolType.HAND, ToolFunction.DRAWING)
//        }
//
//        binding.eraserSwitch.setOnCheckedChangeListener { _, isChecked ->
//            val toolType = if (binding.penSwitch.isChecked) ToolType.PEN else ToolType.HAND
//            if (isChecked) {
//                myscript.setPointerTool(toolType, ToolFunction.ERASING)
//            } else {
//                myscript.setPointerTool(toolType, ToolFunction.DRAWING)
//            }
//        }
//
//        binding.redo.setOnClickListener {
//            myscript.redo()
//        }
//
//        binding.undo.setOnClickListener {
//            myscript.undo()
//        }
//    }

}