package com.knowre.android.myscript.iink

import com.myscript.iink.Configuration


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