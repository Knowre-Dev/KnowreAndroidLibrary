package com.knowre.android.myscript.iink

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.myscript.iink.Engine
import com.myscript.iink.uireferenceimplementation.EditorBinding
import com.myscript.iink.uireferenceimplementation.EditorView
import com.myscript.iink.uireferenceimplementation.FontUtils
import kotlinx.coroutines.CoroutineScope
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets




class MyScriptBuilder {

    fun build(
        context: Context,
        certificate: ByteArray,
        editorView: EditorView,
        folders: FolderProviderApi,
        assetResource: MyScriptAssetResource,
        convertingStandbyJobScope: CoroutineScope
    ) : MyScriptApi {
        folders.rootFolder.deleteRecursively()

        val engine = Engine.create(certificate)
            .apply { deletePackage(folders.packageFolder) }
        val editorData = EditorBinding(engine, context.provideTypefaces())
            .openEditor(editorView)

        with(folders) {
            configFolder.mkdirs()
            mathResourceFolder.mkdirs()
        }

        return MyScript(
            engine = engine,
            configFolder = folders.configFolder.apply { mkdirs() },
            contentPackageTempFolder = folders.contentPackageTempFolder,
            packageFolder = folders.packageFolder,
            editor = editorData.editor!!,
            inputController = editorData.inputController!!,
            grammar = Grammar(
                configFolder = folders.configFolder,
                assetResource = assetResource,
                mathResourceFolder = folders.mathResourceFolder
            ),
            scope = convertingStandbyJobScope
        )
            .apply { setTheme(context.resources.theme()) }
    }

    private fun Resources.theme(): String {
        return openRawResource(R.raw.theme).use { input ->
            ByteArrayOutputStream().use { output ->
                input.copyTo(output)
                output.toString(StandardCharsets.UTF_8.name())
            }
        }
    }

    private fun Context.provideTypefaces(): Map<String, Typeface> {
        val typefaces = FontUtils.loadFontsFromAssets(assets) ?: mutableMapOf()
        ResourcesCompat.getFont(this, R.font.symbola)?.let {
            typefaces["SYMBOLA"] = it
        }
        return typefaces
    }

}