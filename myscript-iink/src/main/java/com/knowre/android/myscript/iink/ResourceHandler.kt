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
    }

    private val configFile = File(configFolder, "math.conf")

    private val mathConfigTemplate = { grammarName: String ->
        """
            Bundle-Version: 1.0
            Bundle-Name: math
            Configuration-Script:
             AddResDir ../resources
    
            Name: standard
            Type: Math
            Configuration-Script:
             AddResource math/math-ak.res
             AddResource math/$grammarName
        """
            .trimIndent()
    }

    init {
        with(context) {
            copyAssetFileTo(assetFileName = "resources/math/$MATH_AK_RESOURCE_NAME", outputFile = File(mathResourceFolder, MATH_AK_RESOURCE_NAME))
            copyAssetFileTo(assetFileName = "resources/math/$DEFAULT_GRAMMAR_NAME", outputFile = File(mathResourceFolder, DEFAULT_GRAMMAR_NAME))
        }

        setGrammar(file = null)
    }

    /**
     * Math grammar 를 [file] 로 변경한다. null 일 경우 기본 그래머로 설정한다.
     */
    fun setGrammar(file: File?) {
        file
            ?.let {
//                file.copyTo(File(mathResourceFolder, file.name), overwrite = true)
                configFile.writeText(mathConfigTemplate(file.name))
            }
            ?: run { configFile.writeText(mathConfigTemplate(DEFAULT_GRAMMAR_NAME)) }
    }

}