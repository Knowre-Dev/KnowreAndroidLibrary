package com.knowre.android.myscript.iink

import com.myscript.iink.Configuration


/**
 * https://developer.myscript.com/docs/interactive-ink/2.1/reference/configuration/ 에서 general section 부분의 설정 값을 설정한다.
 * 아래 주석이 없는 함수들의 동작에 대해서는 위 홈페이지를 참조.
 */
internal class GeneralConfiguration constructor(private val configuration: Configuration) {

    fun setConfigFilePath(vararg paths: String): GeneralConfiguration {
        configuration.setStringArray("configuration-manager.search-path", paths)
        return this
    }

    fun setContentPackageTempFolder(path: String): GeneralConfiguration {
        configuration.setString("content-package.temp-folder", path)
        return this
    }

}