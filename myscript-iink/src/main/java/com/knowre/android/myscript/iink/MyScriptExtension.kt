package com.knowre.android.myscript.iink

import com.myscript.iink.Editor
import com.myscript.iink.EditorError


fun MyScriptApi.addListener(
    onInterpreted: (interpreted: String) -> Unit = { _ -> },
    onInterpretError: (editor: Editor, blockId: String, error: EditorError, message: String) -> Unit = { _, _, _, _ -> },
    onImportError: () -> Unit =  {  }
) {
    addListener(object : MyScriptInterpretListener {
        override fun onInterpreted(interpreted: String) {
            onInterpreted(interpreted)
        }

        override fun onInterpretError(editor: Editor, blockId: String, error: EditorError, message: String) {
            onInterpretError(editor, blockId, error, message)
        }

        override fun onImportError() {
            onImportError()
        }
    })
}