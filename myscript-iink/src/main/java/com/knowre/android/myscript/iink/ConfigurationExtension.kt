package com.knowre.android.myscript.iink

import com.myscript.iink.Configuration


internal fun Configuration.ofGeneral(func: GeneralConfiguration.() -> Unit) = GeneralConfiguration(this).func()

internal fun Configuration.ofMath(func: MathConfiguration.() -> Unit) = MathConfiguration(this).func()

