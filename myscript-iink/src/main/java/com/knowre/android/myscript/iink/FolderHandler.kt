package com.knowre.android.myscript.iink

import android.content.Context
import java.io.File


internal class FolderHandler constructor(private val rootFolder: File) {

    companion object {
        private const val MY_SCRIPT_FOLDER_NAME = "myscript"
        private const val CONF_FOLDER_SIMPLE_PATH = "/$MY_SCRIPT_FOLDER_NAME/conf"
        private const val MATH_RESOURCE_FOLDER_SIMPLE_PATH = "/$MY_SCRIPT_FOLDER_NAME/resources/math"
        private const val PACKAGE_FOLDER_SIMPLE_PATH = "/$MY_SCRIPT_FOLDER_NAME/package"
        private const val CONTENT_PACKAGE_TEMP_FOLDER_SIMPLE_PATH = "/$MY_SCRIPT_FOLDER_NAME/tmp"

        fun getInitial(context: Context) = FolderHandler(context.filesDir)
    }

    val configFolder
        get() = File(rootFolder, CONF_FOLDER_SIMPLE_PATH)

    val mathResourceFolder
        get() = File(rootFolder, MATH_RESOURCE_FOLDER_SIMPLE_PATH)

    val packageFolder
        get() = File(rootFolder, PACKAGE_FOLDER_SIMPLE_PATH)

    val contentPackageTempFolder
        get() =File(rootFolder, CONTENT_PACKAGE_TEMP_FOLDER_SIMPLE_PATH)

    init {
        configFolder.mkdirs()
        mathResourceFolder.mkdirs()
    }

}