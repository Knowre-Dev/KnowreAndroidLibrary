package com.knowre.android.myscript.iink

import com.myscript.iink.ContentPackage
import com.myscript.iink.ContentPart
import com.myscript.iink.Editor
import com.myscript.iink.Engine
import com.myscript.iink.IEditorListener
import com.myscript.iink.PointerTool
import com.myscript.iink.uireferenceimplementation.InputController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


/**
 * force pen 으로 설정해야 pen 이든 touch 이든지 상관없이 drawing/erasing 을 할 수 있게 된다. force pen 이라고해서 pen 으로만 drawing/erasing 한다라고 생각하면 안된다.
 * force pen 은 drawing/erasing 모드 force touch 는 drawing 이 아니라 gesture detecting 모드라고 생각하면 된다.
 * 참고 : pen 으로 drawing 할지 touch 로 drawing 할지는 editor.toolController.setToolForType(..) 로 설정한다.
 */
private const val DRAWING_OR_ERASING_ONLY = InputController.INPUT_MODE_FORCE_PEN

/**
 * AUTO 일 경우 pen 으로 터치할 경우 force pen(drawing mode) 이되고 손으로 터치 할 경우 force touch(gesture detecting mode) 가 된다.
 * (참고 : 현재 앱에서는 이 상태를 사용하지 않지만. 기능 자체는 넣어두었다.)
 */
private const val DRAWING_OR_ERASING_BY_PEN_BUT_GESTURE_BY_HAND = InputController.INPUT_MODE_AUTO

private const val MATH_PART_NAME = "Math"

private const val CONVERT_STANDBY_DELAY: Long = 100

internal class MyScript(
    packageFolder: File,
    private val rootFolder: File,
    private val engine: Engine,
    private val inputController: InputController,
    private val editor: Editor,
    private val mathGrammar: MathGrammar,
    private val scope: CoroutineScope

) : MyScriptApi {

    private var listener: MyScriptInterpretListener? = null

    private var contentPackage: ContentPackage = engine.createPackage(packageFolder)
    private var contentPart: ContentPart = contentPackage.createPart(MATH_PART_NAME)
        set(value) {
            contentPackage.removePart(field)
            field.close()
            editor.part = value
            field = value
        }

    private var convertStandby: Job? = null

    private var lastInterpretedLaTex: String = ""

    private var isAutoConvertEnabled: Boolean = true

    /**
     * 현재 마이스크립트는 touch up 하면 자동으로 컨버팅 해주는데, 이는 undo, redo 시에도 마찬가지이다.
     * undo redo 시에 자동으로 컨버팅하면, 빠르게 undo redo 를 실행할 시 제대로 실행되지 않는 경우가 발생한다.
     * 때문에, 이 변수로 undo redo 로 인한 [IEditorListener.contentChanged] 발생 시 컨버팅을 하지 않도록 컨트롤 한다.
     */
    private var shouldPreventAutoConvertTemporarily: Boolean = false
    private val shouldAutoConvert
        get() = isAutoConvertEnabled && !shouldPreventAutoConvertTemporarily

    init {
        with(editor) {
            addListener(
                contentChanged = { editor, _ ->
                    editor.latex()
                        .also { if (lastInterpretedLaTex == it) return@addListener }
                        .also { lastInterpretedLaTex = it }
                        .also { listener?.onInterpreted(it) }

                    convertStandby?.cancel()
                    if (shouldAutoConvert) {
                        convertStandby = scope.launch {
                            delay(CONVERT_STANDBY_DELAY)
                            convert()
                        }
                    }
                },
                onError = { _, _, editorError, message -> listener?.onError(editorError, message) }
            )
            part = contentPart
        }

        with(inputController) {
            inputMode = DRAWING_OR_ERASING_ONLY
            setOnTouchListener { _, _ ->
                shouldPreventAutoConvertTemporarily = false
            }
        }
    }

    override fun undo() {
        shouldPreventAutoConvertTemporarily = true
        editor.undo()
    }

    override fun redo() {
        shouldPreventAutoConvertTemporarily = true
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

    override fun isIdle() = editor.isIdle

    override fun isAutoConvertEnabled(isEnabled: Boolean) {
        isAutoConvertEnabled = isEnabled
    }

    /**
     * 참고 : 현재 앱에서는 펜 색 변경을 사용하고 있지 않다.
     */
    override fun setPenColor(color: Int) {
        runCatching {
            editor.toolController
                .setToolStyle(PointerTool.PEN, style(colorValue((color.opaque.iinkColor))))
        }
            .onFailure { /** if failure, a pointer event sequence is in progress, not allowed to re-configure or change tool, currently do nothing */ }
    }

    /**
     * 참고 : 현재 앱에서는 [ToolType.HAND], [ToolFunction.DRAWING] 만 사용 중이다.
     */
    override fun setPointerTool(toolType: ToolType, toolFunction: ToolFunction) {
        when (toolType) {
            ToolType.PEN -> inputController.inputMode = DRAWING_OR_ERASING_BY_PEN_BUT_GESTURE_BY_HAND
            ToolType.HAND -> inputController.inputMode = DRAWING_OR_ERASING_ONLY
        }

        editor.toolController.setToolForType(toolType.toPointerType, toolFunction.toPointerTool)
    }

    override fun setTheme(theme: String) {
        editor.theme = theme
    }

    override fun setInterpretListener(listener: MyScriptInterpretListener) {
        this.listener = listener
    }

    /**
     * Math grammar 를 [byteArray] 로 변경한 후 현재 part 를 닫고 새로운 part 를 만들어 할당한다.
     *
     * [MathGrammar.load] 에서 math config 파일을 변경해 그래머를 변경하게 되는데,
     * math config 는 [ContentPart] 가 [ContentPackage] 에 할당되기 전에 한번 설정되면,
     * 그 이후에는 새로운 [contentPart] 를 만들어 붙이지 않는 이상 다이나믹하게 변경이 불가능하다.
     * 때문에 config 가 변경될 경우 부득이하게(그래머 변경은 config 변경을 필요로한다.),
     * 현재 [ContentPart] 를 close 하고 새로운 [ContentPart] 를 만들어 [ContentPackage] 에 붙혀야 한다.
     *
     * @param grammarName .res 를 제외한 그래머 이름
     * @param byteArray 그래머 파일의 byte 값
     *
     * @see contentPart
     * @see [MathGrammar.load]
     */
    override fun loadMathGrammar(grammarName: String, byteArray: ByteArray) {
        mathGrammar.load("$grammarName.res", byteArray)
        contentPart = contentPackage.createPart(MATH_PART_NAME)
    }

    override fun close() {
        contentPart.close()
        contentPackage.close()
        editor.renderer.close()
        editor.close()
        engine.close()
        rootFolder.deleteRecursively()
        scope.cancel()
    }

}