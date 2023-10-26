package com.knowre.android.myscript.iink

import com.knowre.android.myscript.iink.view.style
import com.myscript.iink.ContentPackage
import com.myscript.iink.ContentPart
import com.myscript.iink.Editor
import com.myscript.iink.Engine
import com.myscript.iink.PointerTool
import com.myscript.iink.uireferenceimplementation.InputController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * force pen 으로 설정해야 pen 이든 touch 이든지 상관없이 drawing 을 할 수 있게 된다. force pen 이라고해서 pen 으로만 drawing 한다라고 생각하면 안된다.
 * force pen 은 drawing 모드 force touch 는 drawing 이 아니라 gesture detecting 모드라고 생각하면 된다.
 * 참고 : pen 으로 drawing 할지 touch 로 drawing 할지는 editor.toolController.setToolForType(..) 로 설정한다.
 */
private const val DRAWING = InputController.INPUT_MODE_FORCE_PEN

/**
 * AUTO 일 경우 pen 으로 터치할 경우 force pen(drawing mode) 이되고 손으로 터치 할 경우 force touch(gesture detecting mode) 가 된다.
 */
private const val DRAWING_BY_PEN_GESTURE_BY_HAND = InputController.INPUT_MODE_AUTO

private const val MATH_PART_NAME = "Math"


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

    private var listener: MyScriptInterpretListener? = null
    private var contentPackage: ContentPackage = engine.createPackage(packageFolder)
        .also { contentPart = it.createPart(MATH_PART_NAME) }
    private var contentPart: ContentPart
    private var convertStandby: Job? = null
    private var lastInterpretedLaTex: String = ""

    init {
        engine.configuration
            .ofGeneral()
            .setConfigFilePath(configFolder.path)
            .setContentPackageTempFolder(contentPackageTempFolder.path)

        with(editor) {
            configuration
                .ofMath()
                .isMathSolverEnable(false)
                .isConvertAnimationEnable(true)
                .setSessionTime(100)

            addListener(
                contentChanged = { editor, _ ->
                    editor.latex()
                        .also { if (lastInterpretedLaTex == it) return@addListener }
                        .also {
                            lastInterpretedLaTex = it
                            listener?.onInterpreted(it)
                        }

                    convertStandby?.cancel()
                    convertStandby = scope.launch {
                        delay(100)
                        convert()
                    }
                },
                onError = { _, _, _, message -> listener?.onError(message) }
            )

            part = contentPart
        }

        inputController.inputMode = DRAWING
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

    override fun getCurrentLatex() = editor.latex()

    override fun canRedo(): Boolean = editor.canRedo()

    override fun canUndo(): Boolean = editor.canUndo()

    override fun setPenColor(color: Int) {
        editor.toolController.setToolStyle(PointerTool.PEN, style(colorValue((color.opaque.iinkColor))))
            .runCatching { /** if failure, a pointer event sequence is in progress, not allowed to re-configure or change tool, currently do nothing */ }
    }

    override fun setPointerTool(toolType: ToolType, toolFunction: ToolFunction) {
        when (toolType) {
            ToolType.PEN -> inputController.inputMode = DRAWING_BY_PEN_GESTURE_BY_HAND
            ToolType.HAND -> inputController.inputMode = DRAWING
        }

        editor.toolController.setToolForType(toolType.toPointerType, toolFunction.toPointerTool)
    }

    /**
     * Math grammar 를 [file] 로 변경한 후 현재 part 를 닫고 새로운 part 를 만들어 할당한다.
     *
     * [ResourceHandler.setGrammar] 에서 math config 파일을 변경해 그래머를 변경하게 되는데,
     * math config 는 [ContentPart] 가 [ContentPackage] 에 할당되기 전에 한번 설정되면, 그 이후에는 다이나믹하게 변경이 불가능하다.
     * 때문에 config 가 변경될 경우 부득이하게, 현재 [ContentPart] 를 close 하고 새로운 [ContentPart] 를 만들어 [ContentPackage] 에 붙혀야 한다.
     *
     * @see [ResourceHandler.setGrammar]
     */
    override fun setGrammar(file: File?) {
        resourceManager.setGrammar(file)
        contentPackage.let { contentPackage ->
            contentPart.let { part ->
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

    override fun close() {
        contentPart.close()
        contentPackage.close()
        editor.renderer.close()
        editor.close()
    }

}