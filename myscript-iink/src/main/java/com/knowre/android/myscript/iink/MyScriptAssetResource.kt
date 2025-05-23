package com.knowre.android.myscript.iink

import android.content.Context
import android.content.res.AssetManager


class MyScriptAssetResource(context: Context) {

    val acknowledgeByte by lazy {
        context.assets
            .toByteArray("$RESOURCE_FOLDER/$MATH_FOLDER/$MATH_AK_RESOURCE_NAME")
    }

    val defaultGrammarByte by lazy {
        context.assets
            .toByteArray("$RESOURCE_FOLDER/$MATH_FOLDER/$DEFAULT_GRAMMAR_NAME")
    }

    private fun AssetManager.toByteArray(fileName: String) = open(fileName).use { it.readBytes() }

    companion object {
        const val MATH_AK_RESOURCE_NAME = "math-ak.res"
        const val DEFAULT_GRAMMAR_NAME = "math-grm-standard.res"
        private const val RESOURCE_FOLDER = "resources"
        private const val MATH_FOLDER = "math"
    }
}