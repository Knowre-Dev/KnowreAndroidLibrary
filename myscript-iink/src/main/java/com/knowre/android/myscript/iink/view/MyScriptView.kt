package com.knowre.android.myscript.iink.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.knowre.android.myscript.iink.Folders
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptModule
import com.knowre.android.myscript.iink.ResourceHandler
import com.knowre.android.myscript.iink.certificate.MyCertificate
import com.knowre.android.myscript.iink.databinding.ViewMyscriptBinding
import com.myscript.iink.Engine
import com.myscript.iink.uireferenceimplementation.EditorBinding
import com.myscript.iink.uireferenceimplementation.EditorView
import com.myscript.iink.uireferenceimplementation.FontUtils
import com.myscript.iink.uireferenceimplementation.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets


class MyScriptView constructor(
    context: Context,
    attrs: AttributeSet? = null

) : FrameLayout(context, attrs) {

    private val binding = ViewMyscriptBinding.inflate(LayoutInflater.from(context), this, true)
    private val editorView = findViewById<EditorView>(R.id.editor_view)

    private val engine = Engine.create(MyCertificate.getBytes())
    private val editorData = EditorBinding(engine, provideTypefaces()).openEditor(editorView)
    private val folders = Folders.getInitial(context)
    private val scope = MainScope()

    private val myScriptModule: MyScriptModule = MyScriptModule(
        engine = engine,
        configFolder = folders.configFolder,
        contentPackageTempFolder = folders.contentPackageTempFolder,
        packageFolder = folders.packageFolder,
        editor = editorData.editor!!,
        inputController = editorData.inputController!!,
        resourceManager = ResourceHandler(
            context,
            configFolder = folders.configFolder,
            mathResourceFolder = folders.mathResourceFolder
        ),
        scope = scope
    )
        .also { it.setTheme(theme()) }

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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        api().close()
        scope.cancel()
    }

    fun api(): MyScriptApi = myScriptModule

    private fun theme(): String {
        return context.resources.openRawResource(com.knowre.android.myscript.iink.R.raw.theme).use { input ->
            ByteArrayOutputStream().use { output ->
                input.copyTo(output)
                output.toString(StandardCharsets.UTF_8.name())
            }
        }
    }

    private fun provideTypefaces(): Map<String, Typeface> {
        val typefaces = FontUtils.loadFontsFromAssets(context.assets) ?: mutableMapOf()
        ResourcesCompat.getFont(context, com.knowre.android.myscript.iink.R.font.symbola)?.let {
            typefaces["SYMBOLA"] = it
        }
        return typefaces
    }

}