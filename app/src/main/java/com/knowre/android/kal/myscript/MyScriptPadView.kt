package com.knowre.android.kal.myscript

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.knowre.android.kal.databinding.ViewMyscriptPadBinding
import com.knowre.android.myscript.iink.MyScriptInterpretListener
import java.io.File


internal class MyScriptPadView constructor(
    context: Context,
    attrs: AttributeSet? = null

) : FrameLayout(context, attrs) {

    private val binding = ViewMyscriptPadBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.convert.setOnClickListener { binding.myScript.api().convert() }
        binding.deleteAll.setOnClickListener { binding.myScript.api().deleteAll() }
        binding.myScript.api().setInterpretListener(object : MyScriptInterpretListener {
            override fun onInterpreted(interpreted: String) {
                Log.d("MY_LOG", "interpreted $interpreted")
            }

            override fun onError(message: String) {
                Log.d("MY_LOG", "onError $message")
            }
        })
    }

    fun setGrammar(file: File) {
        binding.myScript.api().setGrammar(file)
    }

}