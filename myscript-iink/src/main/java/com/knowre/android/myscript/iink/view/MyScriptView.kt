package com.knowre.android.myscript.iink.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.knowre.android.myscript.iink.databinding.ViewMyscriptBinding
import com.myscript.iink.uireferenceimplementation.EditorView
import com.myscript.iink.uireferenceimplementation.R


class MyScriptView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewMyscriptBinding.inflate(LayoutInflater.from(context), this, true)
    val editorView: EditorView = findViewById(R.id.editor_view)
    val candidateView: CandidateView = binding.candidateView

    init {
        with(editorView) {
            post {
                renderer?.apply {
                    setViewOffset(0f, 0f)
                    viewScale = 1f
                }
                visibility = View.VISIBLE
            }
        }
    }
}