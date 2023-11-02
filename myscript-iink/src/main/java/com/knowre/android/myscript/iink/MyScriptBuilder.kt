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
import java.io.File
import java.nio.charset.StandardCharsets


/**
 * @see [MathConfiguration.sessionTimeMillis]
 */
private const val INTERPRET_SESSION_TIME_MILLIS: Long = 100

class MyScriptBuilder(
    val context: Context,
    certificate: ByteArray,
    editorView: EditorView,
    val folders: FolderProviderApi,
    val assetResource: MyScriptAssetResource,
    val convertingStandbyJobScope: CoroutineScope

) {

    val engine = Engine.create(certificate)
        .apply { deletePackage(folders.packageFolder) }

    private val editorData = EditorBinding(engine, context.provideTypefaces())
        .openEditor(editorView)

    val editor = editorData.editor!!

    private val inputController = editorData.inputController!!

    private val mathResourceConfiger = MathResourceConfiger(folders.configFolder)

    private val mathGrammar = MathGrammar(
        mathResourceFolder = folders.mathResourceFolder,
        mathResourceConfiger = mathResourceConfiger
    )
        .also { it.load(MyScriptAssetResource.DEFAULT_GRAMMAR_NAME, assetResource.defaultGrammarByte) }

    fun build(): MyScriptApi {
        return MyScript(
            engine = engine,
            packageFolder = folders.packageFolder,
            rootFolder = folders.rootFolder,
            editor = editor,
            inputController = inputController,
            mathGrammar = mathGrammar,
            scope = convertingStandbyJobScope
        )
            .apply { setTheme(context.resources.theme()) }
    }

}

fun MyScriptBuilder.deleteRootFolder(): MyScriptBuilder {
    folders.rootFolder.deleteRecursively()
    return this
}

fun MyScriptBuilder.createNecessaryFolders(): MyScriptBuilder {
    folders.configFolder.mkdirs()
    folders.mathResourceFolder.mkdirs()
    return this
}

fun MyScriptBuilder.setGeneralConfiguration(
    configPath: String = folders.configFolder.path,
    contentPackageTempFolder: String = folders.contentPackageTempFolder.path
): MyScriptBuilder {
    engine.configuration
        .ofGeneral {
            this.configPath = configPath
            this.contentPackageTempFolder = contentPackageTempFolder
        }
    return this
}

fun MyScriptBuilder.setMathConfiguration(
    isMathSolverEnabled: Boolean = false,
    isConvertAnimationEnabled: Boolean = true,
    sessionTimeMilis: Long = INTERPRET_SESSION_TIME_MILLIS
): MyScriptBuilder {
    editor.configuration
        .ofMath {
            this.isMathSolverEnabled = isMathSolverEnabled
            this.isConvertAnimationEnabled = isConvertAnimationEnabled
            this.sessionTimeMillis = sessionTimeMilis
        }
    return this
}

fun MyScriptBuilder.writeAssetResourcesToMathResourceFolder(): MyScriptBuilder {
    with(assetResource) {
        defaultGrammarByte
            .also { File(folders.mathResourceFolder, MyScriptAssetResource.DEFAULT_GRAMMAR_NAME).writeBytes(it) }
        acknowledgeByte
            .also { File(folders.mathResourceFolder, MyScriptAssetResource.MATH_AK_RESOURCE_NAME).writeBytes(it) }
    }
    return this
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