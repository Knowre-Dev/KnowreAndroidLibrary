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

class MyScriptInitializer(
    val context: Context,
    certificate: ByteArray,
    editorView: EditorView,
    val folders: FolderProviderApi,
    val assetResource: MyScriptAssetResource,
    val convertingStandbyJobScope: CoroutineScope

) {

    private val engine = Engine.create(certificate)
        .apply { deletePackage(folders.packageFolder) }

    private val editorData = EditorBinding(engine, context.provideTypefaces())
        .openEditor(editorView)

    private val editor = editorData.editor!!

    private val inputController = editorData.inputController!!

    private val mathResourceConfiger = MathResourceConfiger(folders.configFolder)

    private val mathGrammar = MathGrammar(
        mathResourceFolder = folders.mathResourceFolder,
        mathResourceConfiger = mathResourceConfiger
    )
        .also { it.load(MyScriptAssetResource.DEFAULT_GRAMMAR_NAME, assetResource.defaultGrammarByte) }

    fun deleteRootFolder(): MyScriptInitializer {
        folders.rootFolder.deleteRecursively()
        return this
    }

    fun createNecessaryFolders(): MyScriptInitializer {
        folders.configFolder.mkdirs()
        folders.mathResourceFolder.mkdirs()
        return this
    }

    fun setGeneralConfiguration(
        configPath: String = folders.configFolder.path,
        contentPackageTempFolder: String = folders.contentPackageTempFolder.path
    ): MyScriptInitializer {
        engine.configuration
            .ofGeneral {
                this.configPath = configPath
                this.contentPackageTempFolder = contentPackageTempFolder
            }
        return this
    }

    fun setMathConfiguration(
        isMathSolverEnabled: Boolean = false,
        isConvertAnimationEnabled: Boolean = true,
        sessionTimeMillis: Long = INTERPRET_SESSION_TIME_MILLIS
    ): MyScriptInitializer {
        editor.configuration
            .ofMath {
                this.isMathSolverEnabled = isMathSolverEnabled
                this.isConvertAnimationEnabled = isConvertAnimationEnabled
                this.sessionTimeMillis = sessionTimeMillis
            }
        return this
    }

    fun writeAssetResourcesToMathResourceFolder(): MyScriptInitializer {
        with(assetResource) {
            defaultGrammarByte
                .also { File(folders.mathResourceFolder, MyScriptAssetResource.DEFAULT_GRAMMAR_NAME).writeBytes(it) }
            acknowledgeByte
                .also { File(folders.mathResourceFolder, MyScriptAssetResource.MATH_AK_RESOURCE_NAME).writeBytes(it) }
        }
        return this
    }

    fun initialize(): MyScriptApi {
        return MyScript(
            engine = engine,
            packageFolder = folders.packageFolder,
            rootFolder = folders.rootFolder,
            editor = editor,
            inputController = inputController,
            mathGrammar = mathGrammar,
            scope = convertingStandbyJobScope
        )
            .apply { theme = context.resources.theme() }
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