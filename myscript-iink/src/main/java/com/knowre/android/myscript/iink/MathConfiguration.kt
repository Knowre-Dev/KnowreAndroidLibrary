package com.knowre.android.myscript.iink

import com.myscript.iink.Configuration
import com.myscript.iink.Editor
import com.myscript.iink.IEditorListener
import kotlin.properties.Delegates


/**
 * https://developer.myscript.com/docs/interactive-ink/2.1/reference/configuration/ 에서 math section 부분의 설정 값을 설정한다.
 * 아래 주석이 없는 동작에 대해서는 위 홈페이지를 참조.
 */
internal class MathConfiguration constructor(private val configuration: Configuration) {

    companion object {
        const val CONFIG_BUNDLE_NAME_DEFAULT = "math"
        const val CONFIG_NAME_DEFAULT = "standard"
    }

    var mathConfigurationBundle by Delegates.observable(CONFIG_BUNDLE_NAME_DEFAULT) { _, _, new ->
        configuration.setString("math.configuration.bundle", new)
    }

    var mathConfigurationName by Delegates.observable(CONFIG_NAME_DEFAULT) { _, _, new ->
        configuration.setString("math.configuration.name", new)
    }

    /**
     * 사용자가 Touch up 한 후, [IEditorListener.contentChanged] 가 불리기 까지는 마스에 기본적으로 800~1000ms 정도의 interval 이 세팅돼 있다.
     * https://developer.myscript.com/docs/interactive-ink/2.1/reference/configuration/ 에서 diagram.session-time 으로 검색해보면 설명이 나와있다.
     * (다만 왜 math.session-time 은 doc 에는 없는지는 모르겠다. doc 를 업데이트 안한 건지.)
     * 이를, 적당히 줄여야 touch up 후에 빠른시간안에 인식이 된다. 만약 이 값을 기본 값을 쓰게되면 이 interval 시간동안에는 [Editor.export_] 를 통해 그린 스트록과 일치하는 latex 값을 뽑아 올 수 없게되고
     * 사용자가 저 interval 시간 사이에 정답확인과 같은 동작을 할 경우 잘못된 latex 값이 서버로 전송되게 된다.
     * (마이스크립트 측에서는 이 값을 줄일 경우 (자기들도 정확히 설명해줄 순 없지만..) 사이드 이펙트가 있을 순 있으니 적절히 줄여보고 사이드 이펙트가 없는 선에서 값을 설정하라고 안내해줌)
     */
    var sessionTimeMillis by Delegates.observable(100L) { _, _, new ->
        configuration.setNumber("math.session-time", new)
    }

    /**
     * true 로 설정하면, [MyScript.convert] 실행 시 변환된 수학 공식에 정답도 같이 표시된다.(latex 결과 값도 변하니 주의)
     */
    var isMathSolverEnabled by Delegates.observable(false) { _, _, new ->
        configuration.setBoolean("math.solver.enable", new)
    }

    var isConvertAnimationEnabled by Delegates.observable(true) { _, _, new ->
        configuration.setBoolean("math.convert.animate", new)
    }

}