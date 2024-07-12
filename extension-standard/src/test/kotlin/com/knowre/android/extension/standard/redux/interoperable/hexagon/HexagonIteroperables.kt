package com.knowre.android.extension.standard.redux.interoperable.hexagon


internal interface HexagonStateType
internal interface HexagonViewStateAware
internal interface HexagonStateAware<VS : HexagonViewStateAware> : HexagonStateType {
    val viewState: VS
}

internal interface HexagonActionType
internal interface HexagonCallbackAction : HexagonActionType
internal interface HexagonMiddlewareAction : HexagonActionType

internal interface HexagonReducer<S : HexagonStateType> {
    fun reduce(state: S, action: HexagonActionType): S
}