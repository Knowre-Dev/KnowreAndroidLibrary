package com.knowre.android.myscript.iink

import androidx.annotation.ColorInt
import com.myscript.iink.PointerTool
import java.io.File


interface MyScriptApi {
    fun undo()
    fun redo()
    fun deleteAll()
    fun setGrammar(file: File?)
    fun setPenColor(@ColorInt color: Int)
    fun setPointerTool(pointerTool: PointerTool)
    fun convert()
    fun getCurrentLatex(): String
    fun canRedo(): Boolean
    fun canUndo(): Boolean
    fun setInterpretListener(listener: MyScriptInterpretListener)
    fun close()
}