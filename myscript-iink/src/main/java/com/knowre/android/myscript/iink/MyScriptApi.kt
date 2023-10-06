package com.knowre.android.myscript.iink

import androidx.annotation.ColorInt
import java.io.File


interface MyScriptApi {
    fun undo()
    fun redo()
    fun deleteAll()
    fun setGrammar(file: File?)
    fun setPenColor(@ColorInt color: Int)
    fun setPointerTool(toolType: ToolType, isHandDrawingAllowed: Boolean)
    fun convert()
    fun getCurrentLatex(): String
    fun canRedo(): Boolean
    fun canUndo(): Boolean
    fun setInterpretListener(listener: MyScriptInterpretListener)
    fun close()
}