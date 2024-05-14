package com.knowre.android.myscript.iink.view

import androidx.core.view.isVisible
import com.knowre.android.myscript.iink.MyScriptApi
import com.knowre.android.myscript.iink.MyScriptInterpretListener
import com.knowre.android.myscript.iink.jiix.Jiix
import com.knowre.android.myscript.iink.jiix.isValid
import com.myscript.iink.Editor
import com.myscript.iink.EditorError


class StrokeSelectionBasicControl(
    private val strokeSelectionView: StrokeSelectionView,
    private val myScript: MyScriptApi,
    private val strokeSelectListener: StrokeSelectionView.Listener?
) {

    init {
        myScript.addListener(object : MyScriptInterpretListener {
            override fun onInterpreted(interpreted: String) {
                strokeSelectionView.isVisible = false
            }

            override fun onInterpretError(editor: Editor, blockId: String, error: EditorError, message: String) {
                strokeSelectionView.isVisible = false
            }

            override fun onImportError() {
                strokeSelectionView.isVisible = false
            }
        })

        strokeSelectionView.listener = object : StrokeSelectionView.Listener {
            override fun onJiixChanged(jiix: Jiix) {
                strokeSelectionView.isVisible = false
                myScript.import(jiix)
                myScript.convert()

                strokeSelectListener?.onJiixChanged(jiix)
            }

            override fun onViewHidden() {
                strokeSelectListener?.onViewHidden()
            }
        }
    }

    fun enableStrokeSelectionMode(isEnable: Boolean, onFailure: ((MyScriptApi.StrokeSelectionModeError) -> Unit)?) {
        if (isEnable) {
            myScript.convert()
            val jiix = myScript.getJiix()
            if (!jiix.isValid()) {
                onFailure?.let { it(MyScriptApi.StrokeSelectionModeError.INVALID_STROKE) }
                return
            }
            if (!myScript.isIdle) {
                onFailure?.let { it((MyScriptApi.StrokeSelectionModeError.EDITOR_BUSY)) }
                return
            }
            strokeSelectionView.show(jiix)
        } else {
            strokeSelectionView.isVisible = false
        }
    }
}