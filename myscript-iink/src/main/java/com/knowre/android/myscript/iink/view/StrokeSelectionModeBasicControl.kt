package com.knowre.android.myscript.iink.view

import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.addListener
import com.knowre.android.myscript.iink.jiix.changeItem
import com.knowre.android.myscript.iink.jiix.changeLabel
import com.knowre.android.myscript.iink.jiix.isValid


class StrokeSelectionModeBasicControl(
    strokeSelectListener: StrokeSelectionView.Listener?,
    private val strokeSelectionView: StrokeSelectionView,
    private val myScript: MyScriptApi
) {

    init {
        myScript.addListener(
            onInterpreted = {
                strokeSelectionView.hide()
            },
            onInterpretError = { _, _, _, _ ->
                strokeSelectionView.hide()
            },
            onImportError = {
                strokeSelectionView.hide()
            }
        )

        strokeSelectionView.listener = strokeSelectListener
    }

    fun enable(onFailure: ((StrokeSelectionModeError) -> Unit)?) {
        with(myScript) {
            checkStrokeSelectionAvailable()
                .onSuccess {
                    convert()
                    strokeSelectionView.show(it)
                }
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