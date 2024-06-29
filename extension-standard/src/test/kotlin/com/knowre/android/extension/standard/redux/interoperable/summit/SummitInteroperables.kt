package com.knowre.android.extension.standard.redux.interoperable.summit

import com.knowre.android.extension.standard.redux.Reducible
import com.knowre.android.extension.standard.redux.operation.ReduceOperation
import com.knowre.android.extension.standard.redux.spec.ReduceSpec
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// ---------- interoperable interfaces (e.g. Summit-Android KnowRedux) ----------
internal interface SummitStateType
internal interface SummitViewStateType
internal interface SummitViewStateAware<VS : SummitViewStateType> : SummitStateType {
    val viewState: VS
}

internal interface SummitAction
internal interface SummitViewAction
internal interface SummitViewRenderAction : SummitViewAction
internal interface SummitViewCallbackAction : SummitViewAction
internal interface SummitStateAction<R : SummitViewRenderAction, C : SummitViewCallbackAction> :
    SummitAction {
    val renderAction: R? get() = null
    val callbackAction: C? get() = null
}

internal interface SummitReducer<S : SummitStateType, in A : SummitAction> {
    fun reduce(state: S, action: A): S
}

// ---------- interop-bridge interfaces (e.g. Summit-Android KnowRedux) ----------
internal abstract class StateReduceSpec<R, VS : SummitViewStateType>(snapshot: R) :
    ReduceSpec<R>(snapshot) where R : SummitViewStateAware<VS>, R : Reducible {

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