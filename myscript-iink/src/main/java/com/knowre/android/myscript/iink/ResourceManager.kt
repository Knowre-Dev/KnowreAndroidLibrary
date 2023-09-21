package com.knowre.android.myscript.iink

import android.content.Context
import java.io.File


internal class ResourceManager constructor(
    private val context: Context

) {

    private val simpleResourcePath = "/resource/math"
    val configFileDirectory: File = context.cacheDir
    val resourceFileDirectory = File(configFileDirectory, simpleResourcePath).apply { mkdirs() }

    lateinit var configFile: File

    fun prepare() {
        context.copyAssetFileTo(assetFileName = "resources/math/math-ak.res", outputFile = File(resourceFileDirectory, "math-ak.res"))
        context.copyAssetFileTo(assetFileName = "resources/math/math-grm-standard.res", outputFile = File(resourceFileDirectory, "math-grm-standard.res"))
//        setConfigFile()
    }

    fun setConfigFile(grammarName: String = "math-grm-standard.res") {
        configFile = File(configFileDirectory, "math.conf").apply {
            writeText(createMathConfig("standard", grammarName))
        }
    }

    fun createMathConfig(configName: String, grammarName: String) = """
            Bundle-Version: 1.0
            Bundle-Name: math
            Configuration-Script:
             AddResDir ./resources
    
            Name: $configName
            Type: Math
            Configuration-Script:
             AddResource math/math-ak.res
             AddResource math/$grammarName
        """
        .trimIndent()

}