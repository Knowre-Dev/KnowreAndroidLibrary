package com.knowre.android.myscript.iink

import com.knowre.android.myscript.iink.MyScriptAssetResource.Companion.DEFAULT_GRAMMAR_NAME
import java.io.File


internal class MathGrammar constructor(
    private val mathResourceFolder: File,
    private val mathResourceConfiger: MathResourceConfiger

) {

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