package com.knowre.android.myscript.iink.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.knowre.android.myscript.iink.Folders
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptModule
import com.knowre.android.myscript.iink.ResourceHandler
import com.knowre.android.myscript.iink.databinding.ViewMyscriptBinding
import com.myscript.iink.uireferenceimplementation.FontUtils
import com.myscript.iink.uireferenceimplementation.R
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets


class MyScriptView constructor(
    context: Context,
    attrs: AttributeSet? = null

) : FrameLayout(context, attrs) {

    private val binding = ViewMyscriptBinding.inflate(LayoutInflater.from(context), this, true)

    private val folders = Folders.getInitial(context)

    private val myScriptModule: MyScriptModule = MyScriptModule(
        editorView = findViewById(R.id.editor_view),
        theme = theme(),
        typefaces = provideTypefaces(),
        resourceManager = ResourceHandler(
            context,
            configFolder = folders.configFolder,
            mathResourceFolder = folders.mathResourceFolder
        ),
        folders = folders
    )

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        api().close()
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