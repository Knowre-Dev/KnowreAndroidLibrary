package com.knowre.android.myscript.iink

import com.knowre.android.myscript.iink.MyScriptAssetResource.Companion.DEFAULT_GRAMMAR_NAME
import com.knowre.android.myscript.iink.MyScriptAssetResource.Companion.MATH_AK_RESOURCE_NAME
import java.io.File


internal class MathGrammar constructor(
    configFolder: File,
    assetResource: MyScriptAssetResource,
    private val mathResourceFolder: File

) {

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
        assetResource.defaultGrammarByte
            .also { File(mathResourceFolder, DEFAULT_GRAMMAR_NAME).writeBytes(it) }

        assetResource.acknowledgeByte
            .also { File(mathResourceFolder, MATH_AK_RESOURCE_NAME).writeBytes(it) }

        load(DEFAULT_GRAMMAR_NAME, assetResource.defaultGrammarByte)
    }

    fun load(grammarName: String, byteArray: ByteArray) {
        runCatching {
            byteArray.run {
                File(mathResourceFolder, grammarName)
                    .apply {
                        if (!exists()) {
                            createNewFile()
                            writeBytes(this@run)
                        }
                    }
            }
        }
            .onSuccess { it.let { configFile.writeText(mathConfigTemplate(it.name)) } }
            .onFailure { configFile.writeText(mathConfigTemplate(DEFAULT_GRAMMAR_NAME)) }
    }

}