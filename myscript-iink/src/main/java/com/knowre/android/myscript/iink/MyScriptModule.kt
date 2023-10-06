package com.knowre.android.myscript.iink

import com.knowre.android.myscript.iink.view.style
import com.myscript.iink.ContentPackage
import com.myscript.iink.ContentPart
import com.myscript.iink.Editor
import com.myscript.iink.Engine
import com.myscript.iink.MimeType
import com.myscript.iink.PointerTool
import com.myscript.iink.PointerType
import com.myscript.iink.uireferenceimplementation.InputController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


internal class MyScriptModule(
    engine: Engine,
    configFolder: File,
    contentPackageTempFolder: File,
    packageFolder: File,
    private val inputController: InputController,
    private val editor: Editor,
    private val resourceManager: ResourceHandler,
    private val scope: CoroutineScope

) : MyScriptApi {

    companion object {
        private const val MATH_PART_NAME = "Math"
    }

    private var listener: MyScriptInterpretListener? = null

    private var contentPackage: ContentPackage? = null
    private var contentPart: ContentPart? = null

    private var convertStandby: Job? = null

    private var lastInterpretedLaTex: String = ""

    init {
        engine.configuration
            .ofGeneral()
            .setConfigFilePath(configFolder.path)
            .setContentPackageTempFolder(contentPackageTempFolder.path)

        contentPackage = engine.createPackage(packageFolder)
            .also { contentPart = it.createPart(MATH_PART_NAME) }

        with(editor) {
            configuration
                .ofMath()
                .isMathSolverEnable(false)
                .isConvertAnimationEnable(true)

            addListener(
                contentChanged = { editor, _ ->
                    val interpretedLaTex = editor.export_(null, MimeType.LATEX)
                    if (lastInterpretedLaTex == interpretedLaTex) return@addListener
                    lastInterpretedLaTex = interpretedLaTex

                    listener?.onInterpreted(lastInterpretedLaTex)

                    convertStandby?.cancel()
                    convertStandby = scope.launch {
                        delay(1000)
                        convert()
                    }
                },
                onError = { _, _, _, message -> listener?.onError(message) }
            )

            part = contentPart
        }

        inputController.inputMode = InputController.INPUT_MODE_FORCE_PEN
    }

    override fun undo() {
        editor.undo()
    }

    override fun redo() {
        editor.redo()
    }

    override fun deleteAll() {
        editor.clear()
    }

    override fun convert() {
        convertStandby?.cancel()
        editor.let { it.convert(null, it.getSupportedTargetConversionStates(null)[0]) }
    }

    override fun getCurrentLatex() = editor.export_(null, MimeType.LATEX)

    override fun canRedo(): Boolean = editor.canRedo()

    override fun canUndo(): Boolean = editor.canUndo()

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
        resourceManager.setGrammar(file)
        contentPackage?.let { contentPackage ->
            contentPart?.let { part ->
                contentPackage.removePart(part)
                part.close()
            }
            contentPart = contentPackage.createPart(MATH_PART_NAME)
        }
        editor.part = contentPart
    }

    override fun setTheme(theme: String) {
        editor.theme = theme
    }

    override fun setInterpretListener(listener: MyScriptInterpretListener) {
        this.listener = listener
    }

    private fun pointerType(isHandDrawingAllowed: Boolean, pointerTool: PointerTool): PointerType {
        return if (!isHandDrawingAllowed) {
            inputController.inputMode = InputController.INPUT_MODE_AUTO
            if (pointerTool == PointerTool.HAND) PointerType.TOUCH else PointerType.PEN
        } else {
            inputController.inputMode = if (pointerTool == PointerTool.HAND) InputController.INPUT_MODE_FORCE_TOUCH else InputController.INPUT_MODE_FORCE_PEN
            PointerType.PEN
        }
    }

    override fun close() {
        contentPart?.close()
        contentPackage?.close()
        editor.renderer.close()
        editor.close()
    }

}