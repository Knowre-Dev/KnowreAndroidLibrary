package com.knowre.android.myscript.iink

import java.io.File


const val CONFIG_FILE_NAME = "math.conf"

internal class MathConfig constructor(private val configFolder: File) {

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

    val write = { grammarName: String ->
        File(configFolder, CONFIG_FILE_NAME).writeText(template(grammarName))
    }

}