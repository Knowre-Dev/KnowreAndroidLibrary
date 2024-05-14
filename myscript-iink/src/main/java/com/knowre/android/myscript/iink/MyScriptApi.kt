package com.knowre.android.myscript.iink

import com.knowre.android.myscript.iink.jiix.Jiix
import com.knowre.android.myscript.iink.view.StrokeSelectionBasicControl
import com.knowre.android.myscript.iink.view.StrokeSelectionView


interface MyScriptApi {
    data class Tool(val toolType: ToolType, val toolFunction: ToolFunction) {

        companion object {
            val DEFAULT = Tool(ToolType.HAND, ToolFunction.DRAWING)
        }
    }

    enum class StrokeSelectionModeError {
        INVALID_STROKE, EDITOR_BUSY
    }

    var isAutoConvertEnabled: Boolean
    var theme: String
    var tool: Tool
    var penColor: Int
    val jiixJson: String
    val currentLatex: String
    val isIdle: Boolean
    val canUndo: Boolean
    val canRedo: Boolean
    fun addListener(listener: MyScriptInterpretListener)
    fun removeListener(listener: MyScriptInterpretListener)
    fun getJiix(): Jiix
    fun undo()
    fun redo()
    fun eraseAll(keepRedoUndoStack: Boolean = false)
    fun convert()
    fun import(jiix: Jiix)
    fun import(json: String)
    fun connectStrokeSelection(view: StrokeSelectionView, listener: StrokeSelectionView.Listener?): StrokeSelectionBasicControl
    fun loadMathGrammar(grammarName: String, byteArray: ByteArray)
    fun close()
}