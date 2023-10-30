package com.knowre.android.myscript.iink

import java.io.File


interface FolderProviderApi {
    val rootFolder: File
    val configFolder: File
    val mathResourceFolder: File
    val packageFolder: File
    val contentPackageTempFolder: File
}