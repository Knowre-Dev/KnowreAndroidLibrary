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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.properties.Delegates


/**
 * force pen 으로 설정해야 pen 이든 touch 이든지 상관없이 drawing/erasing 을 할 수 있게 된다. force pen 이라고해서 pen 으로만 drawing/erasing 한다라고 생각하면 안된다.
 * force pen 은 drawing/erasing 모드 force touch 는 drawing 이 아니라 gesture detecting 모드라고 생각하면 된다.
 * 참고 : pen 으로 drawing 할지 touch 로 drawing 할지는 editor.toolController.setToolForType(..) 로 설정한다.
 */
private const val DRAWING_OR_ERASING_ONLY = InputController.INPUT_MODE_FORCE_PEN

/**
 * AUTO 일 경우 pen 으로 터치할 경우 force pen(drawing mode) 이되고 손으로 터치 할 경우 force touch(gesture detecting mode) 가 된다.
 * (참고 : 현재 앱에서는 이 상태를 사용하지 않지만. 기능 자체는 넣어 두었다.)
 */
private const val DRAWING_OR_ERASING_BY_PEN_BUT_GESTURE_BY_HAND = InputController.INPUT_MODE_AUTO

/**
 * [ContentPart] 를 어떤 Type 의 마이스크립트 기능으로 사용할지를 나타낸다. 이는 변경되면 안된다.
 * 미리 정의된 Text, Math, Drawing, Diagram, Raw Content, Text Document 중 하나여야하며 우리 앱에서는 Math 만 사용한다.
 */
private const val MATH_PART_NAME = "Math"

private const val CONVERT_STANDBY_DELAY: Long = 100

internal class MyScript(
    packageFolder: File,
    private val rootFolder: File,
    private val engine: Engine,
    private val inputController: InputController,
    private val editor: Editor,
    private val mathGrammarLoader: MathGrammarLoader,
    private val scope: CoroutineScope

) : MyScriptApi {

    override var listener: MyScriptInterpretListener? = null

    override var isAutoConvertEnabled: Boolean = true

    override var theme: String by Delegates.observable("") { _, _, new ->
        editor.theme = new
    }

    /**
     * 참고 : 현재 앱에서는 [ToolType.HAND], [ToolFunction.DRAWING] 만 사용 중이다.
     */
    override var tool: MyScriptApi.Tool by Delegates.observable(MyScriptApi.Tool.DEFAULT) { _, _, _ ->
        when (tool.toolType) {
            ToolType.PEN -> inputController.inputMode = DRAWING_OR_ERASING_BY_PEN_BUT_GESTURE_BY_HAND
            ToolType.HAND -> inputController.inputMode = DRAWING_OR_ERASING_ONLY
        }

        editor.toolController.setToolForType(tool.toolType.toPointerType, tool.toolFunction.toPointerTool)
    }

    /**
     * 0xFF0000(RED)의 예시와 같이 RGB 를 표현하는 수로 값을 넣어줘야 한다.
     * 참고 : 현재 앱에서는 펜 색 변경을 사용하고 있지 않다.
     */
    override var penColor: Int by Delegates.observable(0) { _, _, new ->
        runCatching {
            editor.toolController
                .setToolStyle(PointerTool.PEN, style(colorValue((new.opaque.iinkColor))))
        }
            .onFailure { /** if failure, a pointer event sequence is in progress, not allowed to re-configure or change tool, currently do nothing */ }
    }

    override val currentLatex: String
        get() = editor.latex()

    override val isIdle: Boolean
        get() = editor.isIdle

    override val canUndo: Boolean
        get() = editor.canUndo()

    override val canRedo: Boolean
        get() = editor.canRedo()

    private var contentPackage: ContentPackage = engine.createPackage(packageFolder)
    private var contentPart: ContentPart = contentPackage.createPart(MATH_PART_NAME)
        set(value) {
            contentPackage.removePart(field)
            field.close()
            editor.part = value
            field = value
        }

    private var lastInterpretedLaTex: String = ""

    private var convertStandby: Job? = null

    private val shouldAutoConvert
        get() = isAutoConvertEnabled && !shouldPreventAutoConvertTemporarily

    /**
     * 현재 마이스크립트는 touch up 하면 자동으로 컨버팅 해주는데, 이는 undo, redo 시에도 마찬가지이다.
     * undo redo 시에 자동으로 컨버팅하면, 빠르게 undo redo 를 실행할 시 제대로 동작하지 않는 이슈가 발생한다.
     * 때문에, 이 변수로 undo redo 로 인한 [IEditorListener.contentChanged] 발생 시 자동 컨버팅을 하지 않도록 조절 한다.
     */
    private var shouldPreventAutoConvertTemporarily: Boolean = false

    init {
        with(editor) {
            addEditorListener()
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

    /**
     * Math grammar 를 [byteArray] 로 변경한 후 현재 part 를 닫고 새로운 part 를 만들어 할당한다.
     *
     * [MathGrammarLoader.load] 에서 math config 파일을 변경해 그래머를 변경하게 되는데,
     * math config 는 [ContentPart] 가 [ContentPackage] 에 할당되기 전에 한번 설정되면,
     * 그 이후에는 새로운 [contentPart] 를 만들어 붙이지 않는 이상 다이나믹하게 변경이 불가능하다.
     * 때문에 config 가 변경될 경우 부득이하게(그래머 변경은 config 변경을 필요로한다.),
     * 현재 [ContentPart] 를 close 하고 새로운 [ContentPart] 를 만들어 [ContentPackage] 에 붙혀야 한다.
     *
     * @param grammarName 확장자(.res)를 제외한 그래머 이름
     * @param byteArray 그래머 파일의 byte 값
     *
     * @see contentPart
     * @see [MathGrammarLoader.load]
     */
    override fun loadMathGrammar(grammarName: String, byteArray: ByteArray) {
        mathGrammarLoader.load("$grammarName.res", byteArray)
        contentPart = contentPackage.createPart(MATH_PART_NAME)
    }

    override fun close() {
        contentPart.close()
        contentPackage.close()
        editor.renderer.close()
        editor.close()
        engine.close()
        rootFolder.deleteRecursively()
        convertStandby?.cancel()
    }

    private fun Editor.addEditorListener() {
        addListener(
            contentChanged = contentChangedListener(),
            onError = onEditorError()
        )
    }

    private fun contentChangedListener(): ContentChanged = { editor, _ ->
        val latex = editor.latex()

        /**
         * 스크록이 변화하여 [ContentChanged] 가 불렸는데, 현재의 latex 가 마지막으로 인식된 latex 와 값이 같으면, converting 작업을 따로 하지 않아야한다.
         * 원래는 이와 같은 처리가 없어도 보통 문제가 없으나, 마이스크립트가 인식하기 애매한 스트록을 인식 시킬 경우,
         * (예, "2 > 3" 처럼 좌, 우 항 모두 존재하는게 아니라 "> 3" 과 같이 우항만 존재하는 스트록)
         * [ContentChanged] 가 계속해서 여러번 불리거나, 컨버팅된 글자에 이상현상이 생기는 등 사이드 이펙트가 발생한다.
         */
        if (lastInterpretedLaTex != latex) {
            lastInterpretedLaTex = latex
            listener?.onInterpreted(latex)

            convertStandby?.cancel()
            if (shouldAutoConvert) {
                convertStandby = scope.launch {
                    delay(CONVERT_STANDBY_DELAY)
                    convert()
                }
            }
        }
    }

    private fun onEditorError(): OnError = { editor, blockId, error, message -> listener?.onError(editor, blockId, error, message) }

}