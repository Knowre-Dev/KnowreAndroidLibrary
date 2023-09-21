package com.knowre.android.myscript.iink


interface MyScriptInterpretListener {
    fun onInterpreted(interpreted: String)
    fun onError(message: String)
}