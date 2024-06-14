package com.knowre.android.extension.standard.redux.interoperable

import com.knowre.android.extension.standard.redux.*
import com.knowre.android.extension.standard.redux.operation.ReduceOperation
import com.knowre.android.extension.standard.redux.operation.reduceOnceOp
import com.knowre.android.extension.standard.redux.operation.reduceOp
import com.knowre.android.extension.standard.redux.spec.ReduceSpec
import com.knowre.android.extension.standard.redux.spec.ReduceSpecDsl
import com.knowre.android.extension.standard.redux.state.ReduceOnce
import com.knowre.android.extension.standard.redux.state.ReducibleValue

// ---------- Combined StateTypes ----------
internal data class CombinedState(
    override val viewState: CombinedViewState = CombinedViewState(),
    val fooState: FooState = FooState(),
    val barState: BarState = BarState(),
    val baz: ReducibleValue<Int> = ReducibleValue.Uninitialized
) : ViewStateAware<CombinedViewState>, Reducible

internal data class CombinedViewState(
    val combined: ReducibleValue<Boolean> = ReducibleValue.Uninitialized
) : ViewStateType, Reducible

internal data class FooState(
    override val viewState: FooViewState = FooViewState(),
    val fooVal1: ReducibleValue<Int> = ReducibleValue.Uninitialized,
    val fooVal2: ReducibleValue<String> = ReducibleValue.Uninitialized
) : ViewStateAware<FooViewState>, Reducible

internal data class FooViewState(
    val fooViewStateVal: ReducibleValue<Boolean> = ReducibleValue(true)
) : ViewStateType, Reducible

internal data class BarState(
    override val viewState: BarViewState = BarViewState(),
    val barVal: ReduceOnce<Boolean> = ReducibleValue.Uninitialized
) : ViewStateAware<BarViewState>, Reducible

internal data class BarViewState(
    val barViewStateVal: ReducibleValue<Int> = ReducibleValue(0)
) : ViewStateType, Reducible

// ---------- Combined ReduceAction ----------
internal interface CombinedReduceAction :
    ReduceAction<CombinedState, CombinedStateReduceSpec>

internal sealed class CombinedAction :
    StateAction<CombinedRenderAction, CombinedCallbackAction> {

    class Callback(
        override val callbackAction: CombinedCallbackAction = CombinedCallbackAction
    ) : CombinedAction()

    class Render(
        override val renderAction: CombinedRenderAction = CombinedRenderAction
    ) : CombinedAction()
}

internal data class UpdateCombinedState(
    private val newCombined: Boolean,
    private val newFooViewStateVal: Boolean,
    private val newFooVal1: Int,
    private val newFooVal2: String,
    private val newBarViewStateVal: Int,
    private val newBarVal: Boolean,
    private val newBaz: Int
) : CombinedAction(), CombinedReduceAction {
    override val operation: CombinedStateReduceSpec.() -> Unit
        get() = {
            viewStateSpec = {
                combined = newCombined
            }
            fooStateSpec = {
                viewStateSpec = {
                    fooViewStateVal = newFooViewStateVal
                }
                fooVal1 = newFooVal1
                fooVal2 = newFooVal2
            }
            barStateSpec = {
                viewStateSpec = {
                    barViewStateVal = newBarViewStateVal
                }
                barVal = newBarVal
            }
            baz = newBaz
        }
}

internal object CombinedRenderAction : ViewRenderAction

internal object CombinedCallbackAction : ViewCallbackAction

// ---------- Combined ReduceSpec ----------
internal class CombinedReducer : Reducer<CombinedState, CombinedAction>,
    SpecBasedReducer<CombinedState, CombinedStateReduceSpec>(::CombinedStateReduceSpec) {

    override fun reduce(state: CombinedState, action: CombinedAction): CombinedState =
        when (action) {
            is CombinedReduceAction -> this(state, action)

            else -> state
        }
}

@ReduceSpecDsl
internal class CombinedStateReduceSpec(snapshot: CombinedState) :
    StateReduceSpec<CombinedState, CombinedViewState>(snapshot) {

    private var viewStateSpecOp: ReduceOperation<CombinedViewStateReduceSpec.() -> Unit> =
        ReduceOperation.Keep
    var viewStateSpec by ViewStateReduceSpec(::viewStateSpecOp::set)

    private var fooStateSpecOp: ReduceOperation<FooStateReduceSpec.() -> Unit> =
        ReduceOperation.Keep
    var fooStateSpec by StateSpecDelegate(::fooStateSpecOp::set)

    private var barStateSpecOp: ReduceOperation<BarStateReduceSpec.() -> Unit> =
        ReduceOperation.Keep
    var barStateSpec by StateSpecDelegate(::barStateSpecOp::set)

    private var bazOp: ReduceOperation<Int> = ReduceOperation.Keep
    var baz by snapshot.baz.reduceOp(::bazOp::set)

    override fun CombinedState.apply(): CombinedState = this
        .update(viewStateSpecOp) { spec ->
            copy(viewState = viewState.reduce(::CombinedViewStateReduceSpec, spec.current))
        }
        .update(fooStateSpecOp) { spec ->
            copy(fooState = fooState.reduce(::FooStateReduceSpec, spec.current))
        }
        .update(barStateSpecOp) { spec ->
            copy(barState = barState.reduce(::BarStateReduceSpec, spec.current))
        }
        .update(bazOp) { new ->
            copy(baz = new)
        }
}

@ReduceSpecDsl
internal class CombinedViewStateReduceSpec(snapshot: CombinedViewState) :
    ReduceSpec<CombinedViewState>(snapshot) {

    private var combinedOp: ReduceOperation<Boolean> = ReduceOperation.Keep
    var combined by snapshot.combined.reduceOp(::combinedOp::set)

    override fun CombinedViewState.apply(): CombinedViewState =
        update(combinedOp) { new ->
            copy(combined = new)
        }
}

@ReduceSpecDsl
internal class FooStateReduceSpec(snapshot: FooState) :
    StateReduceSpec<FooState, FooViewState>(snapshot) {

    private var viewStateSpecOp: ReduceOperation<FooViewStateReduceSpec.() -> Unit> =
        ReduceOperation.Keep
    var viewStateSpec by ViewStateReduceSpec(::viewStateSpecOp::set)

    private var fooVal1Op: ReduceOperation<Int> = ReduceOperation.Keep
    var fooVal1 by snapshot.fooVal1.reduceOp(::fooVal1Op::set)

    private var fooVal2Op: ReduceOperation<String> = ReduceOperation.Keep
    var fooVal2 by snapshot.fooVal2.reduceOp(::fooVal2Op::set)

    override fun FooState.apply(): FooState = this
        .update(viewStateSpecOp) { spec ->
            copy(viewState = viewState.reduce(::FooViewStateReduceSpec, spec.current))
        }
        .update(fooVal1Op) { new ->
            copy(fooVal1 = new)
        }
        .update(fooVal2Op) { new ->
            copy(fooVal2 = new)
        }
}

@ReduceSpecDsl
internal class FooViewStateReduceSpec(snapshot: FooViewState) :
    ReduceSpec<FooViewState>(snapshot) {

    private var fooViewStateValOp: ReduceOperation<Boolean> = ReduceOperation.Keep
    var fooViewStateVal by snapshot.fooViewStateVal.reduceOp(::fooViewStateValOp::set)

    override fun FooViewState.apply(): FooViewState =
        update(fooViewStateValOp) { new ->
            copy(fooViewStateVal = new)
        }
}

@ReduceSpecDsl
internal class BarStateReduceSpec(snapshot: BarState) :
    StateReduceSpec<BarState, BarViewState>(snapshot) {

    private var viewStateSpecOp: ReduceOperation<BarViewStateReduceSpec.() -> Unit> =
        ReduceOperation.Keep
    var viewStateSpec by ViewStateReduceSpec(::viewStateSpecOp::set)

    private var barValOp: ReduceOperation<Boolean> = ReduceOperation.Keep
    var barVal by snapshot.barVal.reduceOnceOp(::barValOp::set)

    override fun BarState.apply(): BarState = this
        .update(viewStateSpecOp) { spec ->
            copy(viewState = viewState.reduce(::BarViewStateReduceSpec, spec.current))
        }
        .updateOnce(barValOp) { new ->
            copy(barVal = new)
        }
}

@ReduceSpecDsl
internal class BarViewStateReduceSpec(snapshot: BarViewState) :
    ReduceSpec<BarViewState>(snapshot) {

    private var barViewStateValOp: ReduceOperation<Int> = ReduceOperation.Keep
    var barViewStateVal by snapshot.barViewStateVal.reduceOp(::barViewStateValOp::set)

    override fun BarViewState.apply(): BarViewState =
        update(barViewStateValOp) { new ->
            copy(barViewStateVal = new)
        }
}