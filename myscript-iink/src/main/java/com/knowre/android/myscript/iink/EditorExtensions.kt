package com.knowre.android.myscript.iink

import com.myscript.iink.ContentPart
import com.myscript.iink.Editor
import com.myscript.iink.EditorError
import com.myscript.iink.IEditorListener
import com.myscript.iink.MimeType


internal fun Editor.latex() = export_(null, MimeType.LATEX)

internal fun Editor.addListener(
    partChanging: (editor: Editor, oldPart: ContentPart?, newPart: ContentPart?) ->Unit = { _, _,_ -> },
    partChanged: (editor: Editor) -> Unit = { _ -> },
    contentChanged: (editor: Editor, blockIds: Array<out String>) -> Unit = { _, _ -> },
    onError: (editor: Editor, blockId: String, error: EditorError, message: String) -> Unit ={ _, _, _ , _ -> },
    selectionChanged: (editor: Editor) -> Unit = { _ -> },
    activeBlockChanged: (editor: Editor, blockId: String) -> Unit = { _, _ -> }
) {
    addListener(object : IEditorListener {
        override fun partChanging(editor: Editor, oldPart: ContentPart?, newPart: ContentPart?) {
            partChanging(editor, oldPart, newPart)
        }

        override fun partChanged(editor: Editor) {
            partChanged(editor)
        }

        override fun contentChanged(editor: Editor, blockIds: Array<out String>) {
            contentChanged(editor, blockIds)
        }

        override fun onError(editor: Editor, blockId: String, error: EditorError, message: String) {
            onError(editor, blockId, error, message)
        }

        override fun selectionChanged(editor: Editor) {
            selectionChanged(editor)
        }

        override fun activeBlockChanged(editor: Editor, blockId: String) {
            activeBlockChanged(editor, blockId)
        }
    })
}