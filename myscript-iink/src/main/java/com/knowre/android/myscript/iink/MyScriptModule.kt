package com.knowre.android.myscript.iink

import android.content.Context
import android.view.View
import com.knowre.android.myscript.iink.certificate.MyCertificate
import com.myscript.iink.Editor
import com.myscript.iink.Engine
import com.myscript.iink.MimeType
import com.myscript.iink.PointerTool
import com.myscript.iink.uireferenceimplementation.EditorBinding
import com.myscript.iink.uireferenceimplementation.EditorView
import com.myscript.iink.uireferenceimplementation.InputController
import java.io.File


internal class MyScriptModule(
    private val editorView: EditorView,
    context: Context

) : MyScriptApi {

    private val engine: Engine = Engine.create(MyCertificate.getBytes())
    private val editorBinding = EditorBinding(engine, mapOf())
    private val editorData = editorBinding.openEditor(editorView)
    private val editor: Editor get() = editorData.editor!!

//    private val resourceManager = ResourceManager(context)

    private val mathResourceSimplePath = "resources/math"
    private val configFileDirectory: File = context.cacheDir
    private val resourceFileDirectory = File(configFileDirectory, mathResourceSimplePath).apply {
        mkdirs()
    }

    private var listener: MyScriptInterpretListener? = null

    private val configFile = File(configFileDirectory, "math.conf").apply {
        writeText(createMathConfig("grammar", "math-grm-standard.res"))
    }

    init {
        context.copyAssetFileTo(assetFileName = "resources/math/math-ak.res", outputFile = File(resourceFileDirectory, "math-ak.res"))
        context.copyAssetFileTo(assetFileName = "resources/math/math-grm-standard.res", outputFile = File(resourceFileDirectory, "math-grm-standard.res"))

        engine.configuration.setStringArray("configuration-manager.search-path", arrayOf(configFileDirectory.path))
        engine.configuration.setString("content-package.temp-folder", configFileDirectory.path + File.separator + "tmp")

        editor.configuration.setString("math.configuration.bundle", "math")
        editor.configuration.setString("math.configuration.name", "grammar")
        editor.configuration.setBoolean("math.solver.enable", true)

        with(editor) {
            addListener(
                contentChanged = { editor, _ ->
                    convert()
                    listener?.onInterpreted(editor.export_(null, MimeType.LATEX))
                },
                onError = { _, _, _, message ->
                    listener?.onError(message)
                }
            )
        }

        editorData.inputController?.inputMode = InputController.INPUT_MODE_FORCE_PEN

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

    override fun undo() {
        editor.undo()
    }

    override fun redo() {
        editor.redo()
    }

    override fun setPenColor(color: Int) {

    }

    override fun setPointerTool(pointerTool: PointerTool) {

    }

    override fun setGrammar(file: File) {
//        file.copyTo(File(resourceFileDirectory, file.name), overwrite = true)
//
//        configFile.writeText("")
//        configFile.writeText(createMathConfig("grammar", file.name))
//
//        editor.configuration
//            .setStringArray("configuration-manager.search-path", arrayOf(configFileDirectory.path))
//        editor.configuration
//            .setString("math.configuration.name", "grammar")

        editor.part = engine
            .createPackage(File(configFileDirectory, "File1.iink"))
            .createPart("Math")
    }

    override fun deleteAll() {
        editor.clear()
    }

    override fun convert() {
        editor.let { it.convert(null, it.getSupportedTargetConversionStates(null)[0]) }
    }

    override fun getCurrentLatex() = editor.export_(null, MimeType.LATEX)

    override fun canRedo(): Boolean = editor.canRedo()

    override fun canUndo(): Boolean = editor.canUndo()

    override fun setInterpretListener(listener: MyScriptInterpretListener) {
        this.listener = listener
    }

    private fun createMathConfig(configName: String, grammarName: String) = """
            Bundle-Version: 1.0
            Bundle-Name: math
            Configuration-Script:
             AddResDir ./resources

            Name: grammar
            Type: Math
            Configuration-Script:
             AddResource math/math-ak.res
             AddResource math/$grammarName
        """
        .trimIndent()

}