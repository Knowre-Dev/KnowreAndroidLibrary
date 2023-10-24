package com.knowre.android.myscript.iink

import androidx.annotation.ColorInt
import java.io.File


interface MyScriptApi {
    fun undo()
    fun redo()
    fun deleteAll()
    fun convert()
    fun getCurrentLatex(): String
    fun canRedo(): Boolean
    fun canUndo(): Boolean
    fun setGrammar(file: File?)
    fun setTheme(theme: String)
    fun setPenColor(@ColorInt color: Int)
    fun setPointerTool(toolType: ToolType, toolFunction: ToolFunction)
    fun setInterpretListener(listener: MyScriptInterpretListener)
    fun close()
}