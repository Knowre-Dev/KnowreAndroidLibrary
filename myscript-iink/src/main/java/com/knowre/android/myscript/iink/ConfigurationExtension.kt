package com.knowre.android.myscript.iink

import com.myscript.iink.Configuration

internal fun Configuration.ofGeneral() = GeneralConfiguration(this)

internal fun Configuration.ofMath() = MathConfiguration(this)

