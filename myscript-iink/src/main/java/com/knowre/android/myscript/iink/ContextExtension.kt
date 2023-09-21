package com.knowre.android.myscript.iink

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


fun Context.copyAssetFileTo(assetFileName: String, outputFile: File): File? {
    try {
        val assetManager = assets
        val inputStream: InputStream = assetManager.open(assetFileName)
        val outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
        return outputFile
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}
