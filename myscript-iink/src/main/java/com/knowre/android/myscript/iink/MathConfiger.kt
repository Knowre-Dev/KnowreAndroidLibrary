package com.knowre.android.myscript.iink

import java.io.File


internal class MathConfiger(private val configFolder: File) {

    private val template = { grammarName: String ->
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

    val grammar = { grammarName: String ->
        File(configFolder, CONFIG_FILE_NAME)
            .writeText(template(grammarName))
    }

    companion object {
        private const val CONFIG_FILE_NAME = "${MathConfiguration.CONFIG_BUNDLE_NAME_DEFAULT}.conf"
    }
}