package com.knowre.android.myscript.iink

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.myscript.iink.Editor
import com.myscript.iink.Engine
import com.myscript.iink.uireferenceimplementation.EditorBinding
import com.myscript.iink.uireferenceimplementation.EditorView
import com.myscript.iink.uireferenceimplementation.FontUtils
import kotlinx.coroutines.CoroutineScope
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets


/**
 * @see [MathConfiguration.setSessionTime]
 */
private const val INTERPRET_SESSION_TIME_MILLIS: Long = 100

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
        val editor = editorData.editor!!
        val inputController = editorData.inputController!!

        with(folders) {
            configFolder.mkdirs()
            mathResourceFolder.mkdirs()
        }

        generalConfiguration(engine, folders)
        mathConfiguration(editorData.editor!!)

        return MyScript(
            engine = engine,
            packageFolder = folders.packageFolder,
            rootFolder = folders.rootFolder,
            editor = editor,
            inputController = inputController,
            mathGrammar = MathGrammar(
                assetResource = assetResource,
                mathResourceFolder = folders.mathResourceFolder,
                mathResourceConfiger = MathResourceConfiger(folders.configFolder)
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

    private fun generalConfiguration(engine: Engine, folders: FolderProviderApi) {
        engine.configuration
            .ofGeneral()
            .setConfigFilePath(folders.configFolder.path)
            .setContentPackageTempFolder(folders.contentPackageTempFolder.path)
    }

    private fun mathConfiguration(editor: Editor) {
        editor.configuration
            .ofMath()
            .isMathSolverEnable(false)
            .isConvertAnimationEnable(true)
            .setSessionTime(INTERPRET_SESSION_TIME_MILLIS)
    }

}