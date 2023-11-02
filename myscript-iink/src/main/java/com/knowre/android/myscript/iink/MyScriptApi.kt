package com.knowre.android.myscript.iink


interface MyScriptApi {
    data class Tool(val toolType: ToolType, val toolFunction: ToolFunction) {
        companion object {
            val DEFAULT = Tool(ToolType.HAND, ToolFunction.DRAWING)
        }
    }

    var listener: MyScriptInterpretListener?
    var isAutoConvertEnabled: Boolean
    var theme: String
    var tool: Tool
    var penColor: Int
    val currentLatex: String
    val isIdle: Boolean
    val canUndo: Boolean
    val canRedo: Boolean
    fun undo()
    fun redo()
    fun deleteAll()
    fun convert()
    fun loadMathGrammar(grammarName: String, byteArray: ByteArray)
    fun close()
}