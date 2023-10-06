package com.knowre.android.myscript.iink

import com.myscript.iink.PointerTool


enum class ToolType {
    PEN, HAND, ERASER
}

internal val ToolType.toPointerTool: PointerTool
    get() = when (this) {
        ToolType.PEN -> PointerTool.PEN
        ToolType.HAND -> PointerTool.HAND
        ToolType.ERASER -> PointerTool.ERASER
    }