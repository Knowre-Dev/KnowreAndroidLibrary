package com.knowre.android.myscript.iink

import android.content.Context
import java.io.File


internal class FolderProvider(val context: Context) {

    val rootFolder
        get() = File(context.filesDir, ROOT)

    val configFolder
        get() = File(rootFolder, CONFIGURATION)

    val mathResourceFolder
        get() = File(rootFolder, MATH_RESOURCE)

    val packageFolder
        get() = File(rootFolder, PACKAGE)

    val contentPackageTempFolder
        get() =File(rootFolder, CONTENT_PACKAGE)

    companion object {
        private const val ROOT = "/myscript"
        private const val CONFIGURATION = "/conf"
        private const val MATH_RESOURCE = "/resources/math"
        private const val PACKAGE = "/package"
        private const val CONTENT_PACKAGE = "/tmp"
    }
}