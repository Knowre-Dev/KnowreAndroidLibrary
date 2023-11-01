package com.knowre.android.myscript.iink

import com.myscript.iink.Configuration
import com.myscript.iink.Editor
import com.myscript.iink.IEditorListener


/**
 * https://developer.myscript.com/docs/interactive-ink/2.1/reference/configuration/ 에서 math section 부분의 설정 값을 설정한다.
 * 아래 주석이 없는 함수들의 동작에 대해서는 위 홈페이지를 참조.
 */
internal class MathConfiguration constructor(private val configuration: Configuration) {

    companion object {
        const val CONFIG_BUNDLE_NAME_DEFAULT = "math"
        const val CONFIG_NAME_DEFAULT = "standard"
    }

    fun setMathConfigurationBundle(bundleName: String = CONFIG_BUNDLE_NAME_DEFAULT): MathConfiguration {
        configuration.setString("math.configuration.bundle", bundleName)
        return this
    }

    fun setMathConfigurationName(configName: String = CONFIG_NAME_DEFAULT): MathConfiguration {
        configuration.setString("math.configuration.name", configName)
        return this
    }

    /**
     * 사용자가 Touch up 한 후, [IEditorListener.contentChanged] 가 불리기 까지는 마스에 기본적으로 800~1000ms 정도가 세팅되어 있다.
     * https://developer.myscript.com/docs/interactive-ink/2.1/reference/configuration/ 에서 diagram.session-time 으로 검색해보면 설명이 나와있다.
     * (다만 왜 math.session-time 는 옵션 적용이되지만 doc 에는 없는지는 모르겠다. doc 를 업데이트 안한 건지.)
     * 이를, 적당히 줄여야 touch up 후에 바로 인식이 된다. 만약 이 값을 기본 값을 쓰게되면 이 interval 시간동안 [Editor.export_] 를 통해 latex 값을 뽑아 올 수 없다.
     */
    fun setSessionTime(millis: Long): MathConfiguration {
        configuration.setNumber("math.session-time", millis)
        return this
    }

    /**
     * true 로 설정하면, [MyScript.convert] 실행 시 변환된 수학 공식에 정답도 같이 표시된다.
     */
    fun isMathSolverEnable(isEnable: Boolean): MathConfiguration {
        configuration.setBoolean("math.solver.enable", isEnable)
        return this
    }

    fun isConvertAnimationEnable(isEnable: Boolean): MathConfiguration {
        configuration.setBoolean("math.convert.animate", isEnable)
        return this
    }

}