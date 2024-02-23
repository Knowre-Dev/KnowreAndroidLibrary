package com.knowre.android.myscript.iink

import com.knowre.android.myscript.iink.MyScriptAssetResource.Companion.DEFAULT_GRAMMAR_NAME
import java.io.File


internal class MathGrammarLoader(
    private val mathResourceFolder: File,
    private val mathConfiger: MathConfiger
) {

    fun load(grammarName: String, byteArray: ByteArray) {
        runCatching {
            File(mathResourceFolder, grammarName)
                .apply { writeBytes(byteArray) }
        }
            .onSuccess { it.let { mathConfiger.grammar(it.name) } }
            .onFailure { mathConfiger.grammar(DEFAULT_GRAMMAR_NAME) }
    }
}