package com.knowre.android.myscript.iink

import com.knowre.android.myscript.iink.MyScriptAssetResource.Companion.DEFAULT_GRAMMAR_NAME
import com.knowre.android.myscript.iink.MyScriptAssetResource.Companion.MATH_AK_RESOURCE_NAME
import java.io.File


internal class MathGrammar constructor(
    assetResource: MyScriptAssetResource,
    private val mathResourceFolder: File,
    private val mathResourceConfiger: MathResourceConfiger

) {

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
            .onSuccess { it.let { mathResourceConfiger.write(it.name) } }
            .onFailure { mathResourceConfiger.write(DEFAULT_GRAMMAR_NAME) }
    }

}