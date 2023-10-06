package com.knowre.android.myscript.iink

import android.content.Context
import java.io.File


internal class ResourceHandler constructor(
    context: Context,
    configFolder: File,
    private val mathResourceFolder: File

) {

    companion object {
        private const val MATH_AK_RESOURCE_NAME = "math-ak.res"
        private const val DEFAULT_GRAMMAR_NAME = "math-grm-standard.res"
        private const val STANDARD_CONFIG_NAME = "standard"
    }

    private val configFile = File(configFolder, "math.conf")

    init {
        with(context) {
            copyAssetFileTo(assetFileName = "resources/math/$MATH_AK_RESOURCE_NAME", outputFile = File(mathResourceFolder, MATH_AK_RESOURCE_NAME))
            copyAssetFileTo(assetFileName = "resources/math/$DEFAULT_GRAMMAR_NAME", outputFile = File(mathResourceFolder, DEFAULT_GRAMMAR_NAME))
        }

        setGrammar(file = null)
    }

    fun setGrammar(file: File?) {
        file?.let {
            file.copyTo(File(mathResourceFolder, file.name), overwrite = true)
            configFile.writeText(createMathConfig(STANDARD_CONFIG_NAME, file.name))
        } ?: run { configFile.writeText(createMathConfig(STANDARD_CONFIG_NAME, DEFAULT_GRAMMAR_NAME)) }
    }

    private fun createMathConfig(configName: String, grammarName: String) = """
            Bundle-Version: 1.0
            Bundle-Name: math
            Configuration-Script:
             AddResDir ../resources
    
            Name: $configName
            Type: Math
            Configuration-Script:
             AddResource math/math-ak.res
             AddResource math/$grammarName
        """
        .trimIndent()

}