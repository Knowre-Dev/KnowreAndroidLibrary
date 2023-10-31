package com.knowre.android.myscript.iink

import com.myscript.iink.IEditorListener


interface MyScriptInterpretListener {
    /**
     * [IEditorListener.contentChanged] 에서 latex 해석이 끝난 후 해석된 결과를 [interpreted] 로 알려준다.
     * 참고 : 해당 함수의 쓰레드는 Main 쓰레드가 아님.
     */
    fun onInterpreted(interpreted: String)
    fun onError(message: String)
}