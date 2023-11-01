package com.knowre.android.myscript.iink

import androidx.annotation.ColorInt


interface MyScriptApi {
    fun undo()
    fun redo()
    fun deleteAll()
    fun convert()
    fun getCurrentLatex(): String
    fun canRedo(): Boolean
    fun canUndo(): Boolean
    fun isIdle(): Boolean
    fun isAutoConvertEnabled(isEnabled: Boolean)
    fun setTheme(theme: String)
    fun setPenColor(@ColorInt color: Int)
    fun setPointerTool(toolType: ToolType, toolFunction: ToolFunction)
    fun setInterpretListener(listener: MyScriptInterpretListener)
    fun loadMathGrammar(grammarName: String, grammar: ByteArray)
    fun close()
}