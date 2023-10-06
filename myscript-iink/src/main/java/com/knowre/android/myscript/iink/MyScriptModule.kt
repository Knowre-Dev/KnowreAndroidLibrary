package com.knowre.android.myscript.iink

import android.graphics.Typeface
import android.util.Log
import android.view.View
import com.knowre.android.myscript.iink.certificate.MyCertificate
import com.knowre.android.myscript.iink.view.style
import com.myscript.iink.ContentPackage
import com.myscript.iink.ContentPart
import com.myscript.iink.Editor
import com.myscript.iink.Engine
import com.myscript.iink.MimeType
import com.myscript.iink.PointerTool
import com.myscript.iink.PointerType
import com.myscript.iink.uireferenceimplementation.EditorBinding
import com.myscript.iink.uireferenceimplementation.EditorView
import com.myscript.iink.uireferenceimplementation.InputController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


internal class MyScriptModule(
    editorView: EditorView,
    theme: String,
    typefaces: Map<String, Typeface>,
    private val resourceManager: ResourceHandler,
    private val folders: Folders

) : MyScriptApi {

    private val engine: Engine = Engine.create(MyCertificate.getBytes())
    private val editorData = EditorBinding(engine, typefaces).openEditor(editorView)
    private val editor: Editor get() = editorData.editor!!

    private var listener: MyScriptInterpretListener? = null

    private var contentPackage: ContentPackage? = null
    private var contentPart: ContentPart? = null

    private var job: Job? = null

    private var lastInterpretedLaTex: String = ""

    init {
        engine.configuration
            .ofGeneral()
            .setConfigFilePath(folders.configFolder.path)
            .setContentPackageTempFolder(folders.contentPackageTempFolder.path)

        editor.configuration
            .ofMath()
            .isMathSolverEnable(false)
            .isConvertAnimationEnable(true)

        editor.theme = theme

        contentPackage = engine.createPackage(folders.packageFolder)
            .also { contentPart = it.createPart("Math") }

        with(editor) {
            addListener(
                contentChanged = { editor, _ ->
                    val interpretedLaTex = editor.export_(null, MimeType.LATEX)

                    if (lastInterpretedLaTex == interpretedLaTex) return@addListener

                    lastInterpretedLaTex = interpretedLaTex

                    listener?.onInterpreted(lastInterpretedLaTex)

                    job?.cancel()
                    job = CoroutineScope(Dispatchers.Main).launch {
                        delay(1000)
                        Log.d("MY_LOG", "convert")
                        convert()
                    }
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

    override fun setPointerTool(toolType: ToolType, isHandDrawingAllowed: Boolean) {
        editor.toolController.setToolForType(pointerType(isHandDrawingAllowed, toolType.toPointerTool), toolType.toPointerTool)
    }

    override fun setGrammar(file: File?) {
        file?.let {
            file.copyTo(File(folders.mathResourceFolder, file.name), overwrite = true)
            resourceManager.setConfigFile(file.name)
        } ?: run { resourceManager.setConfigFile() }

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

    private fun pointerType(isHandDrawingAllowed: Boolean, pointerTool: PointerTool): PointerType {
        return if (!isHandDrawingAllowed) {
            editorData.inputController?.inputMode = InputController.INPUT_MODE_AUTO
            if (pointerTool == PointerTool.HAND) PointerType.TOUCH else PointerType.PEN
        } else {
            editorData.inputController?.inputMode =
                if (pointerTool == PointerTool.HAND) InputController.INPUT_MODE_FORCE_TOUCH else InputController.INPUT_MODE_FORCE_PEN
            PointerType.PEN
        }
    }

    override fun close() {
        closePackage()
        editor.renderer.close()
        editor.close()
    }

    private fun closePackage() {
        contentPart?.close()
        contentPackage?.close()
    }

}