package com.knowre.android.myscript.iink

import android.content.Context
import java.io.File


internal class ResourceHandler constructor(
    private val context: Context,
    private val folderHandler: FolderHandler

) {

    companion object {
        private const val DEFAULT_GRAMMAR_NAME = "math-grm-standard.res"
    }

    init {
        with(context) {
            copyAssetFileTo(assetFileName = "resources/math/math-ak.res", outputFile = File(folderHandler.mathResourceFolder, "math-ak.res"))
            copyAssetFileTo(assetFileName = "resources/math/math-grm-standard.res", outputFile = File(folderHandler.mathResourceFolder, "math-grm-standard.res"))
        }

        setConfigFile()
    }

    fun setConfigFile(grammarName: String = DEFAULT_GRAMMAR_NAME) {
        File(folderHandler.configFolder, "math.conf")
            .apply { writeText(createMathConfig("standard", grammarName)) }
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