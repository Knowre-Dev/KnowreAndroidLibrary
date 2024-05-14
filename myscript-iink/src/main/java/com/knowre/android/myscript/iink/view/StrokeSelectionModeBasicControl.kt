package com.knowre.android.myscript.iink.view

import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptInterpretListener
import com.knowre.android.myscript.iink.jiix.changeItem
import com.knowre.android.myscript.iink.jiix.changeLabel
import com.knowre.android.myscript.iink.jiix.isValid
import com.myscript.iink.Editor
import com.myscript.iink.EditorError


class StrokeSelectionModeBasicControl(
    private val strokeSelectionView: StrokeSelectionView,
    private val myScript: MyScriptApi,
    private val strokeSelectListener: StrokeSelectionView.Listener?
) {

    init {
        myScript.addListener(object : MyScriptInterpretListener {
            override fun onInterpreted(interpreted: String) {
                strokeSelectionView.hide()
            }

            override fun onInterpretError(editor: Editor, blockId: String, error: EditorError, message: String) {
                strokeSelectionView.hide()
            }

            override fun onImportError() {
                strokeSelectionView.hide()
            }
        })

        strokeSelectionView.listener = strokeSelectListener
    }

    fun enable(onFailure: ((StrokeSelectionModeError) -> Unit)?) {
        with(myScript) {
            convert()
            checkStrokeSelectionAvailable()
                .onSuccess { strokeSelectionView.show(it) }
                .onFailure { error ->
                    onFailure?.let {
                        it((error as SelectionUnavailableException).errorType)
                    }
                    strokeSelectionView.hide()
                }
        }
    }

    fun disable() {
        strokeSelectionView.hide()
    }

    fun changeLabel(itemId: String, label: String) {
        with(myScript) {
            import(
                strokeSelectionView.jiix
                    .changeItem(itemId) {
                        changeLabel(label)
                    }
            )
            convert()
        }
        strokeSelectionView.hide()
    }

    private fun MyScriptApi.checkStrokeSelectionAvailable() =
        getJiix().let {
            if (!it.isValid())
                return@let Result.failure(SelectionUnavailableException(StrokeSelectionModeError.INVALID_STROKE))
            if (!isIdle)
                return@let Result.failure(SelectionUnavailableException(StrokeSelectionModeError.EDITOR_BUSY))
            Result.success(it)
        }

    class SelectionUnavailableException(
        val errorType: StrokeSelectionModeError
    ) : Throwable(errorType.toString())
}