package com.knowre.android.myscript.iink

import java.io.File


/**
 * 이 값은 마이스크립트 내부에서 Math 리소스 컨피그 파일을 찾을 때 고정으로 사용하는 값으로 변경하면 안됨
 */
const val CONFIG_FILE_NAME = "math.conf"

internal class MathResourceConfiger constructor(private val configFolder: File) {

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
        File(configFolder, CONFIG_FILE_NAME)
            .writeText(template(grammarName))
    }

}