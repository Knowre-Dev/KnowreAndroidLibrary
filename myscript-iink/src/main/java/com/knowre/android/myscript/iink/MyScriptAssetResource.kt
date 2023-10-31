package com.knowre.android.myscript.iink

import android.content.Context


class MyScriptAssetResource constructor(context: Context) {

    companion object {
        private const val RESOURCE_FOLDER = "resources"
        private const val MATH_FOLDER = "math"
        const val MATH_AK_RESOURCE_NAME = "math-ak.res"
        const val DEFAULT_GRAMMAR_NAME = "math-grm-standard.res"
    }

    val acknowledgeByte = context.assets
        .toByteArray("$RESOURCE_FOLDER/$MATH_FOLDER/$MATH_AK_RESOURCE_NAME")


    val defaultGrammarByte = context.assets
        .toByteArray("$RESOURCE_FOLDER/$MATH_FOLDER/$DEFAULT_GRAMMAR_NAME")

}