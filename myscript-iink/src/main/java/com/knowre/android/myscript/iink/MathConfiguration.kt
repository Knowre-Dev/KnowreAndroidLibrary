package com.knowre.android.myscript.iink

import com.myscript.iink.Configuration


internal class MathConfiguration constructor(private val configuration: Configuration) {

    companion object {
        const val CONFIG_BUNDLE_NAME_DEFAULT = "math"
        const val CONFIG_NAME_DEFAULT = "standard"
    }

    fun setMathConfigurationBundle(bundleName: String = CONFIG_BUNDLE_NAME_DEFAULT): MathConfiguration {
        configuration.setString("math.configuration.bundle", bundleName)
        return this
    }

    fun setMathConfigurationName(configName: String = CONFIG_NAME_DEFAULT): MathConfiguration {
        configuration.setString("math.configuration.name", configName)
        return this
    }

    fun isMathSolverEnable(isEnable: Boolean): MathConfiguration {
        configuration.setBoolean("math.solver.enable", isEnable)
        return this
    }

    fun isConvertAnimationEnable(isEnable: Boolean): MathConfiguration {
        configuration.setBoolean("math.convert.animate", isEnable)
        return this
    }

}