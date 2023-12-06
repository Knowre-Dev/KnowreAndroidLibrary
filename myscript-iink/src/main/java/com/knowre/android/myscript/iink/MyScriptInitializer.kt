package com.knowre.android.myscript.iink

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.myscript.iink.Configuration
import com.myscript.iink.Editor
import com.myscript.iink.Engine
import com.myscript.iink.uireferenceimplementation.EditorBinding
import com.myscript.iink.uireferenceimplementation.EditorView
import com.myscript.iink.uireferenceimplementation.FontUtils
import com.myscript.iink.uireferenceimplementation.InputController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets


/**
 * @see [MathConfiguration.sessionTimeMillis]
 */
private const val INTERPRET_SESSION_TIME_MILLIS: Long = 100

class MyScriptInitializer(
    certificate: ByteArray,
    editorView: EditorView,
    private val context: Context,
    private val folders: FolderProviderApi,
    private val assetResource: MyScriptAssetResource,
    private val scope: CoroutineScope

) {

    private val engine by lazy {
        Engine.create(certificate).apply { deleteUsedPackage(folders.packageFolder) }
    }

    private val editorData by lazy {
        EditorBinding(engine, context.provideTypefaces())
            .openEditor(editorView)
    }

    private val editor: Editor by lazy { editorData.editor!! }

    private val inputController: InputController by lazy { editorData.inputController!! }

    private val mathResourceConfiger = MathResourceConfiger(folders.configFolder)

    private val mathGrammarLoader = MathGrammarLoader(
        mathResourceFolder = folders.mathResourceFolder,
        mathResourceConfiger = mathResourceConfiger
    )

    private var setGeneralConfiguration: (Configuration) -> Unit = { _ -> }
    private var setMathConfiguration: (Configuration) -> Unit = { _ -> }

    fun setGeneralConfiguration(
        configPath: String = folders.configFolder.path,
        contentPackageTempFolder: String = folders.contentPackageTempFolder.path
    ): MyScriptInitializer {
        setGeneralConfiguration = { configuration ->
            configuration
                .ofGeneral {
                    this.configPath = configPath
                    this.contentPackageTempFolder = contentPackageTempFolder
                }
        }
        return this
    }

    fun setMathConfiguration(
        isMathSolverEnabled: Boolean = false,
        isConvertAnimationEnabled: Boolean = true,
        sessionTimeMillis: Long = INTERPRET_SESSION_TIME_MILLIS
    ): MyScriptInitializer {
        setMathConfiguration = { configuration ->
            configuration
                .ofMath {
                    this.mathConfigurationBundle = MathConfiguration.CONFIG_BUNDLE_NAME_DEFAULT
                    this.mathConfigurationName = MathConfiguration.CONFIG_NAME_DEFAULT
                    this.isMathSolverEnabled = isMathSolverEnabled
                    this.isConvertAnimationEnabled = isConvertAnimationEnabled
                    this.sessionTimeMillis = sessionTimeMillis
                }
        }
        return this
    }

    fun initialize(onComplete: (MyScriptApi) -> Unit) {
        scope.launch(Dispatchers.Default) {
            preProcessing()
            setGeneralConfiguration(engine.configuration)

            withContext(Dispatchers.Main) {
                setMathConfiguration(editorData.editor!!.configuration)

                onComplete(
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
                )
            }
        }
    }

    private fun preProcessing() {
        folders.rootFolder.deleteRecursively()
        folders.configFolder.mkdirs()
        folders.mathResourceFolder.mkdirs()
        /**
         * 그래머를 로드 해줘야 math.conf 파일이 생성되기 때문에 초기화 단계에서 선제적으로 이 작업을 수행한다.
         */
        mathGrammarLoader.load(MyScriptAssetResource.DEFAULT_GRAMMAR_NAME, assetResource.defaultGrammarByte)

        with(assetResource) {
            defaultGrammarByte
                .also { File(folders.mathResourceFolder, MyScriptAssetResource.DEFAULT_GRAMMAR_NAME).writeBytes(it) }
            acknowledgeByte
                .also { File(folders.mathResourceFolder, MyScriptAssetResource.MATH_AK_RESOURCE_NAME).writeBytes(it) }
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

}