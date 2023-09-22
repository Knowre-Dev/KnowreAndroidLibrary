package com.knowre.android.myscript.iink

import android.graphics.Typeface
import android.view.View
import com.knowre.android.myscript.iink.certificate.MyCertificate
import com.knowre.android.myscript.iink.view.style
import com.myscript.iink.ContentPackage
import com.myscript.iink.ContentPart
import com.myscript.iink.Editor
import com.myscript.iink.Engine
import com.myscript.iink.MimeType
import com.myscript.iink.PointerTool
import com.myscript.iink.uireferenceimplementation.EditorBinding
import com.myscript.iink.uireferenceimplementation.EditorView
import com.myscript.iink.uireferenceimplementation.InputController
import kotlinx.coroutines.Job
import java.io.File


internal class MyScriptModule(
    editorView: EditorView,
    theme: String,
    typefaces: Map<String, Typeface>,
    private val view: View,
    private val resourceManager: ResourceHandler,
    private val folderHandler: FolderHandler

) : MyScriptApi {

    private var engine: Engine? = Engine.create(MyCertificate.getBytes())
    private val editorData = EditorBinding(engine, typefaces).openEditor(editorView)
    private val editor: Editor get() = editorData.editor!!

    private var listener: MyScriptInterpretListener? = null

    private var contentPackage: ContentPackage? = null
    private var contentPart: ContentPart? = null

    private var job: Job? = null

    init {
        engine!!.configuration
            .ofGeneral()
            .setConfigFilePath(folderHandler.configFolder.path)
            .setContentPackageTempFolder(folderHandler.contentPackageTempFolder.path)

        editor.configuration
            .ofMath()
            .isMathSolverEnable(false)
            .isConvertAnimationEnable(true)

        editor.theme = theme

        contentPackage = engine!!.createPackage(folderHandler.packageFolder)
            .also { contentPart = it.createPart("Math") }

        with(editor) {
            addListener(
                contentChanged = { editor, _ ->
                    listener?.onInterpreted(editor.export_(null, MimeType.LATEX))
                    view.postDelayed({ convert() }, 1000)
                },
                onError = { _, _, _, message ->
                    listener?.onError(message)
                }
            )
            part = contentPart
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
        try {
            editor.toolController.setToolStyle(PointerTool.PEN, style(colorValue((color.opaque.iinkColor))))
        } catch (e: IllegalStateException) {
            // a pointer event sequence is in progress, not allowed to re-configure or change tool
        }
    }

    override fun setPointerTool(pointerTool: PointerTool) {
//        editor.toolController.setToolForType()
    }

    override fun setGrammar(file: File?) {
        file
            ?.let {
                file.copyTo(File(folderHandler.mathResourceFolder, file.name), overwrite = true)
                resourceManager.setConfigFile(file.name)
            }
            ?: run { resourceManager.setConfigFile() }

        contentPackage?.let { contentPackage ->
            contentPart?.let {
                contentPackage.removePart(it)
                it.close()
            }
            contentPart = contentPackage.createPart("Math")
        }

        editor.part = contentPart
    }

    override fun deleteAll() {
        editor.clear()
    }

    override fun convert() {
        job?.cancel()
        editor.let { it.convert(null, it.getSupportedTargetConversionStates(null)[0]) }
    }

    override fun getCurrentLatex() = editor.export_(null, MimeType.LATEX)

    override fun canRedo(): Boolean = editor.canRedo()

    override fun canUndo(): Boolean = editor.canUndo()

    override fun setInterpretListener(listener: MyScriptInterpretListener) {
        this.listener = listener
    }

    override fun close() {
        closePackage()
        editor.renderer.close()
        editor.close()
        engine = null
    }

    private fun closePackage() {
        contentPart?.let { it.close() }
        contentPackage?.let { it.close() }
        contentPart = null
        contentPackage = null
    }

}