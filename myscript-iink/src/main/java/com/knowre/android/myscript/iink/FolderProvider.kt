package com.knowre.android.myscript.iink

import android.content.Context
import java.io.File


class FolderProvider constructor(val context: Context): FolderProviderApi {

    companion object {
        private const val ROOT = "/myscript"
        private const val CONFIGURATION = "/conf"
        private const val MATH_RESOURCE = "/resources/math"
        private const val PACKAGE = "/package"
        private const val CONTENT_PACKAGE = "/tmp"
    }

    override val rootFolder
        get() = File(context.filesDir, ROOT)

    override val configFolder
        get() = File(rootFolder, CONFIGURATION)

    override val mathResourceFolder
        get() = File(rootFolder, MATH_RESOURCE)

    override val packageFolder
        get() = File(rootFolder, PACKAGE)

    override val contentPackageTempFolder
        get() =File(rootFolder, CONTENT_PACKAGE)

}