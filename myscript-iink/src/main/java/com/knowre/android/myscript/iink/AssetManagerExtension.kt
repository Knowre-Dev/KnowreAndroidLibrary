package com.knowre.android.myscript.iink

import android.content.res.AssetManager


fun AssetManager.toByteArray(fileName: String) = open(fileName).use { it.readBytes() }