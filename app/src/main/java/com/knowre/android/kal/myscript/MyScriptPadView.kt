package com.knowre.android.kal.myscript

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.knowre.android.kal.databinding.ViewMyscriptPadBinding
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptInterpretListener
import com.knowre.android.myscript.iink.copyAssetFileTo
import java.io.File


internal class MyScriptPadView constructor(
    context: Context,
    attrs: AttributeSet? = null

) : FrameLayout(context, attrs) {

    private val binding = ViewMyscriptPadBinding.inflate(LayoutInflater.from(context), this, true)

    private val api: MyScriptApi = binding.myScript.api()

    init {
        binding.convert.setOnClickListener { binding.myScript.api().convert() }
        binding.deleteAll.setOnClickListener { binding.myScript.api().deleteAll() }
        binding.myScript.api().setInterpretListener(object : MyScriptInterpretListener {
            override fun onInterpreted(interpreted: String) {
                binding.latex.text = interpreted
                Log.d("MY_LOG", "interpreted $interpreted")
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
    }

}