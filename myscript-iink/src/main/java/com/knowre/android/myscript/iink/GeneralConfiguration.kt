package com.knowre.android.myscript.iink

import com.myscript.iink.Configuration
import kotlin.properties.Delegates

/**
 * https://developer.myscript.com/docs/interactive-ink/2.1/reference/configuration/ 에서 general section 부분의 설정 값을 설정한다.
 * 아래 주석이 없는 동작에 대해서는 위 홈페이지를 참조.
 */
internal class GeneralConfiguration(
    private val configuration: Configuration
) {

    var configPath by Delegates.observable("") { _, _, new ->
        configuration.setStringArray("configuration-manager.search-path", arrayOf(new))
    }

    var contentPackageTempFolder by Delegates.observable("") { _, _, new ->
        configuration.setString("content-package.temp-folder", new)
    }
}