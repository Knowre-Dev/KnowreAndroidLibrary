package com.knowre.android.extension.standard.redux.interoperable

import com.knowre.android.extension.standard.redux.Reducible
import com.knowre.android.extension.standard.redux.operation.ReduceOperation
import com.knowre.android.extension.standard.redux.spec.ReduceSpec
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// ---------- interoperable interfaces (e.g. Summit-Android KnowRedux) ----------
internal interface StateType
internal interface ViewStateType
internal interface ViewStateAware<VS : ViewStateType> : StateType {
    val viewState: VS
}

internal interface Action
internal interface ViewAction
internal interface ViewRenderAction : ViewAction
internal interface ViewCallbackAction : ViewAction
internal interface StateAction<R : ViewRenderAction, C : ViewCallbackAction> : Action {
    val renderAction: R? get() = null
    val callbackAction: C? get() = null
}

internal interface Reducer<S : StateType, in A : Action> {
    fun reduce(state: S, action: A): S
}

// ---------- interop-bridge interfaces (e.g. Summit-Android KnowRedux) ----------
internal abstract class StateReduceSpec<R, VS : ViewStateType>(snapshot: R) :
    ReduceSpec<R>(snapshot) where R : ViewStateAware<VS>, R : Reducible {

    val viewState: VS by snapshot::viewState

    protected class ViewStateReduceSpec<R : Reducible, Spec : ReduceSpec<R>>(
        update: (ReduceOperation.Update<Spec.() -> Unit>) -> Unit
    ) : ReadWriteProperty<Any?, Spec.() -> Unit> by StateSpecDelegate(update)
}

internal class StateSpecDelegate<R : Reducible, Spec : ReduceSpec<R>>(
    private val update: (ReduceOperation.Update<Spec.() -> Unit>) -> Unit
) : ReadWriteProperty<Any?, Spec.() -> Unit> {

    override operator fun getValue(
        thisRef: Any?, property: KProperty<*>
    ): Spec.() -> Unit =
        throw UnsupportedOperationException()

    override operator fun setValue(
        thisRef: Any?, property: KProperty<*>,
        value: Spec.() -> Unit
    ) {
        update(ReduceOperation.Update(value))
    }
}