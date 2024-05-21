package com.knowre.android.myscript.iink

import com.knowre.android.myscript.iink.jiix.Jiix


interface MyScriptApi {
    data class Tool(val toolType: ToolType, val toolFunction: ToolFunction) {

        companion object {
            val DEFAULT = Tool(ToolType.HAND, ToolFunction.DRAWING)
        }
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
    fun loadMathGrammar(grammarName: String, byteArray: ByteArray)
    fun close()
}