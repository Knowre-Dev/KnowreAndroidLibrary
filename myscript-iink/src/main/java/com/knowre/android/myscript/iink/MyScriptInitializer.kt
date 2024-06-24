package com.knowre.android.myscript.iink

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.knowre.android.myscript.iink.view.MyScriptView
import com.myscript.iink.Configuration
import com.myscript.iink.Editor
import com.myscript.iink.Engine
import com.myscript.iink.uireferenceimplementation.EditorBinding
import com.myscript.iink.uireferenceimplementation.FontUtils
import com.myscript.iink.uireferenceimplementation.InputController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets


/**
 * @see [MathConfiguration.sessionTimeMillis]
 */
private const val INTERPRET_SESSION_TIME_MILLIS: Long = 100
private const val CANDIDATE_COUNT: Int = 2

class MyScriptInitializer(
    private val certificate: ByteArray,
    private val myScriptView: MyScriptView,
    private val context: Context,
    private val scope: CoroutineScope
) {

    private val engine by lazy {
        Engine.create(certificate).apply {
            deleteUsedPackage(folders.packageFolder)
        }
    }

    private val editorData by lazy {
        EditorBinding(engine, context.provideTypefaces())
            .openEditor(myScriptView.editorView)
    }

    private val editor: Editor by lazy { editorData.editor!! }

    private val folders = FolderProvider(context)

    private val inputController: InputController by lazy { editorData.inputController!! }

    private val mathConfiger = MathConfiger(folders.configFolder)

    private val mathGrammarLoader = MathGrammarLoader(
        mathResourceFolder = folders.mathResourceFolder,
        mathConfiger = mathConfiger
    )

    private val assetResource = MyScriptAssetResource(context)

    suspend fun initialize(): MyScriptApi {
        withContext(Dispatchers.Default) {
            processResourceAndFolder()
            engine.configuration.generalConfig()
        }

        return withContext(Dispatchers.Main) {
            editor.configuration.mathConfig()
            MyScript(
                engine = engine,
                packageFolder = folders.packageFolder,
                rootFolder = folders.rootFolder,
                editor = editor,
                inputController = inputController,
                mathGrammarLoader = mathGrammarLoader,
                scope = scope
            )
                .apply { theme = context.resources.theme() }
        }
    }

    private fun processResourceAndFolder() {
        with (folders) {
            rootFolder.deleteRecursively()
            configFolder.mkdirs()
            mathResourceFolder.mkdirs()
        }

        /** Grammar 를 로드 해줘야 math.conf 파일이 생성되기 때문에 초기화 단계에서 선제적으로 이 작업을 수행한다. */
        mathGrammarLoader.load(
            grammarName = MyScriptAssetResource.DEFAULT_GRAMMAR_NAME,
            byteArray = assetResource.defaultGrammarByte
        )

        File(folders.mathResourceFolder, MyScriptAssetResource.MATH_AK_RESOURCE_NAME)
            .writeBytes(assetResource.acknowledgeByte)
    }

    private fun Configuration.generalConfig() {
        ofGeneral {
            this.configPath = folders.configFolder.path
            this.contentPackageTempFolder = folders.contentPackageTempFolder.path
        }
    }

    private fun Configuration.mathConfig() {
        ofMath {
            this.mathConfigurationBundle = MathConfiguration.CONFIG_BUNDLE_NAME_DEFAULT
            this.mathConfigurationName = MathConfiguration.CONFIG_NAME_DEFAULT
            this.isMathSolverEnabled = false
            this.isConvertAnimationEnabled = true
            this.sessionTimeMillis = INTERPRET_SESSION_TIME_MILLIS
            this.candidateCount = CANDIDATE_COUNT
        }
    }
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

private fun Engine.deleteUsedPackage(packageFolder: File) {
    runCatching {
        with(packageFolder) {
            openPackage(this)
                .takeIf { !it.isClosed }
                ?.close()
            deletePackage(this)
        }
    }
}