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

    fun enable(onFailure: ((StrokeSelectionModeError) -> Unit)) {
        with(myScript) {
            if (isIdle) {
                convert()
                getJiix().let {
                    if (it.isValid()) {
                        strokeSelectionView.show(it)
                    } else {
                        onFailure(StrokeSelectionModeError.INVALID_STROKE)
                    }
                }
            } else {
                onFailure(StrokeSelectionModeError.EDITOR_BUSY)
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
}