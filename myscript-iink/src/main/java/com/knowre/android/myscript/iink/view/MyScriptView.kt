package com.knowre.android.myscript.iink.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptModule
import com.knowre.android.myscript.iink.databinding.ViewMyscriptBinding
import com.myscript.iink.uireferenceimplementation.R


class MyScriptView constructor(
    context: Context,
    attrs: AttributeSet? = null

) : FrameLayout(context, attrs) {

    private val binding = ViewMyscriptBinding.inflate(LayoutInflater.from(context), this, true)

    private val myScriptModule: MyScriptModule = MyScriptModule(
        context = context,
        editorView = findViewById(R.id.editor_view)
    )

    fun api(): MyScriptApi = myScriptModule

}