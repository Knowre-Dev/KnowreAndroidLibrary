package com.knowre.android.myscript.iink

import com.myscript.iink.PointerTool
import com.myscript.iink.PointerType


/**
 * 화면에 Touch 가 이루어지는 tool 이 무엇인지 나타낸다.
 */
enum class ToolType {
    PEN, HAND
}

/**
 * [ToolType] 으로 touch 가 이루어졌을 경우 해당 touch 영역에 어떠한 동작을 할지를 나타낸다.
 */
enum class ToolFunction {
    DRAWING, ERASING
}

/**
 * 마이스크립트의 [PointerType] 은 화면에 터치하기 위해 사용되는 도구가 무엇인지를 나타낸다.
 * 하지만 무슨 이유에서 인지 [PointerType.PEN] 으로 다 리턴해야. 드로윙이든 지우개든 다 잘 작동한다;
 */
internal val ToolType.toPointerType: PointerType
    get() = when (this) {
        ToolType.PEN -> PointerType.PEN
        ToolType.HAND -> PointerType.PEN
    }

/**
 * 마이스크립트의 [PointerTool] 이란 [PointerType] 로 그려진 터치 정보가 어떤한 목적으로 사용될지를 나타낼지를 의미한다.
 * [ToolType] 으로 그려진 정보가 [ToolFunction] 에 맞게 사용되도록 아래와 같은 정보를 바탕으로 적절한 [PointerTool] 로 변환한다.
 *
 * [PointerTool.PEN] : 해당 터치 정보를 Drawing Stroke 으로 표시함.
 *
 * [PointerTool.HAND] : 해당 터치 정보를 Gesture Detecting(한 칸 띄우기, 도형 정보 선 잇기 등) 에 사용함. 현재 우리 앱에서는 사용하지 않을 내용.
 *
 * [PointerTool.ERASER] : 해당 터치 정보를 content 를 지우는데 사용함.
 *
 * 우리 앱에서는 [PointerTool.HAND] 기능인 Gesture Detecting 을 사용하지 않는다.
 */
internal val ToolFunction.toPointerTool: PointerTool
    get() = when (this) {
        ToolFunction.DRAWING -> PointerTool.PEN
        ToolFunction.ERASING -> PointerTool.ERASER
    }