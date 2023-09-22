package com.knowre.android.myscript.iink.view

import java.util.Locale


/**
 * 참고 : force Locale.US to ensure dotted float formatting (`1.88` instead of `1,88`) whatever the device's locale setup
 */
internal fun style(
    color: String,
    thickness: Float = 0.625F
) = String.format(Locale.US, "" +
    "color: $color;" +
    "-myscript-pen-width: %.2f",
    thickness
)