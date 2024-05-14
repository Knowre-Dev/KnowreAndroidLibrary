package com.knowre.android.myscript.iink

import com.myscript.iink.ContentPart
import com.myscript.iink.Editor
import com.myscript.iink.EditorError
import com.myscript.iink.IEditorListener
import com.myscript.iink.MimeType


internal fun Editor.latex() = export_(null, MimeType.LATEX)

typealias PartChanging = (editor: Editor, oldPart: ContentPart?, newPart: ContentPart?) -> Unit
typealias PartChanged = (editor: Editor) -> Unit
typealias ContentChanged = (editor: Editor, blockIds: Array<out String>) -> Unit
typealias OnError = (editor: Editor, blockId: String, error: EditorError, message: String) -> Unit
typealias SelectionChanged = (editor: Editor) -> Unit
typealias ActiveBlockChanged = (editor: Editor, blockId: String) -> Unit

internal inline fun Editor.addListener(
    crossinline partChanging: PartChanging = { _, _, _ -> },
    crossinline partChanged: PartChanged = { _ -> },
    crossinline contentChanged: ContentChanged = { _, _ -> },
    crossinline onError: OnError = { _, _, _, _ -> },
    crossinline selectionChanged: SelectionChanged = { _ -> },
    crossinline activeBlockChanged: ActiveBlockChanged = { _, _ -> }
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