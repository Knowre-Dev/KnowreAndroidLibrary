package com.knowre.android.extension.standard.redux.interoperable

import com.knowre.android.extension.standard.redux.*
import com.knowre.android.extension.standard.redux.operation.ReduceOperation
import com.knowre.android.extension.standard.redux.operation.reduceOnceOp
import com.knowre.android.extension.standard.redux.operation.reduceOp
import com.knowre.android.extension.standard.redux.spec.ReduceSpec
import com.knowre.android.extension.standard.redux.spec.ReduceSpecDsl
import com.knowre.android.extension.standard.redux.state.ReduceOnce
import com.knowre.android.extension.standard.redux.state.ReducibleValue

// ---------- interoperable Reducible ----------
internal data class InteroperableViewState(
    val count: ReducibleValue<Int> = ReducibleValue.Uninitialized,
    val owner: ReduceOnce<String> = ReducibleValue.Uninitialized
) : ViewStateType, Reducible

internal data class InteroperableState(
    override val viewState: InteroperableViewState = InteroperableViewState(),
    val referenceCount: ReducibleValue<Int> = ReducibleValue(0)
) : ViewStateAware<InteroperableViewState>, Reducible

// ---------- interoperable ReduceAction ----------
internal interface InteroperableReduceAction :
    ReduceAction<InteroperableState, InteroperableStateReduceSpec>

internal sealed class InteroperableAction :
    StateAction<InteroperableRenderAction, InteroperableCallbackAction> {

    object Reset : InteroperableAction(), InteroperableReduceAction {
        override val operation: InteroperableStateReduceSpec.() -> Unit
            get() = {
                viewStateSpec = {
                    count = 0
                }
                referenceCount = 0
            }
    }

    class Callback(
        override val callbackAction: InteroperableCallbackAction
    ) : InteroperableAction()

    object Render : InteroperableAction() {
        override val renderAction = InteroperableRenderAction
    }
}

internal sealed class InteroperableCallbackAction : ViewCallbackAction {
    data class SetCount(private val newCount: Int) :
        InteroperableCallbackAction(), InteroperableReduceAction {
        override val operation: InteroperableStateReduceSpec.() -> Unit
            get() = {
                viewStateSpec = {
                    count = newCount
                }
                referenceCount += 1
            }
    }

    data class SetOwner(private val newOwner: String) :
        InteroperableCallbackAction(), InteroperableReduceAction {
        override val operation: InteroperableStateReduceSpec.() -> Unit
            get() = {
                viewStateSpec = {
                    owner = newOwner
                }
                referenceCount += 1
            }
    }
}

internal object InteroperableRenderAction : ViewRenderAction

// ---------- interoperable ReduceSpec ----------
internal class InteroperableReducer : Reducer<InteroperableState, InteroperableAction>,
    SpecBasedReducer<InteroperableState, InteroperableStateReduceSpec>(
        ::InteroperableStateReduceSpec
    ) {

    override fun reduce(
        state: InteroperableState,
        action: InteroperableAction
    ): InteroperableState = when (action) {
        is InteroperableReduceAction -> this(state, action)

        is InteroperableAction.Callback -> (action.callbackAction as? InteroperableReduceAction)
            ?.let { reduceAction ->
                this(state, reduceAction)
            }
            ?: state

        else -> state
    }
}

@ReduceSpecDsl
internal class InteroperableStateReduceSpec(snapshot: InteroperableState) :
    StateReduceSpec<InteroperableState, InteroperableViewState>(snapshot) {

    private var viewStateSpecOp: ReduceOperation<InteroperableViewStateReduceSpec.() -> Unit> =
        ReduceOperation.Keep
    var viewStateSpec by ViewStateReduceSpec(::viewStateSpecOp::set)

    private var referenceCountOp: ReduceOperation<Int> = ReduceOperation.Keep
    var referenceCount by snapshot.referenceCount.reduceOp(::referenceCountOp::set)

    override fun InteroperableState.apply(): InteroperableState = this
        .update(viewStateSpecOp) { spec ->
            copy(viewState = viewState.reduce(::InteroperableViewStateReduceSpec, spec.current))
        }
        .update(referenceCountOp) { new ->
            copy(referenceCount = new)
        }
}

@ReduceSpecDsl
internal class InteroperableViewStateReduceSpec(snapshot: InteroperableViewState) :
    ReduceSpec<InteroperableViewState>(snapshot) {

    private var countOp: ReduceOperation<Int> = ReduceOperation.Keep
    var count by snapshot.count.reduceOp(::countOp::set)

    private var ownerOp: ReduceOperation<String> = ReduceOperation.Keep
    var owner by snapshot.owner.reduceOnceOp(::ownerOp::set)

    override fun InteroperableViewState.apply(): InteroperableViewState = this
        .update(countOp) { new ->
            copy(count = new)
        }
        .updateOnce(ownerOp) { new ->
            copy(owner = new)
        }
}