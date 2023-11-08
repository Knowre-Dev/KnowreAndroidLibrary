package com.knowre.android.myscript.iink

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.myscript.iink.graphics.Color as IinkColor


@get:ColorInt
internal val IinkColor.androidColor: Int
    get() = android.graphics.Color.argb(a(), r(), g(), b())

internal val Int.iinkColor: IinkColor
    get() {
        val r = this shr 16 and 0xff
        val g = this shr 8 and 0xff
        val b = this and 0xff
        val a = this shr 24 and 0xff
        return IinkColor(r, g, b, a)
    }

@get:ColorInt
internal val Int.opaque: Int
    get() = ColorUtils.setAlphaComponent(this, 0xFF)

internal fun colorValue(iinkColor: IinkColor) = String.format("#%08X", 0xFFFFFFFF and iinkColor.rgba.toLong())