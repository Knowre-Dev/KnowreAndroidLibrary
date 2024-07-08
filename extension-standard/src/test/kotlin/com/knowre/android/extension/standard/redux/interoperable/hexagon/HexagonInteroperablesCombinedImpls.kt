package com.knowre.android.extension.standard.redux.interoperable.hexagon

import com.knowre.android.extension.standard.redux.ReduceAction
import com.knowre.android.extension.standard.redux.Reducible
import com.knowre.android.extension.standard.redux.SpecBasedReducer
import com.knowre.android.extension.standard.redux.interoperable.summit.StateSpecDelegate
import com.knowre.android.extension.standard.redux.operation.ReduceOperation
import com.knowre.android.extension.standard.redux.operation.reduceOp
import com.knowre.android.extension.standard.redux.reduce
import com.knowre.android.extension.standard.redux.spec.ReduceSpec
import com.knowre.android.extension.standard.redux.state.ReducibleValue
import com.knowre.android.extension.standard.redux.update

internal data class ExampleState(
    override val viewState: ExampleViewState = ExampleViewState(),
    val userId: ReducibleValue<Int> = ReducibleValue.Uninitialized
) : HexagonStateAware<ExampleViewState>, Reducible

internal data class ExampleViewState(
    val isShowLoading: ReducibleValue<Boolean> = ReducibleValue.Uninitialized,
    val fooScreenState: ExampleFooScreenState = ExampleFooScreenState(),
    val barScreenState: ExampleBarScreenState = ExampleBarScreenState(),
) : HexagonViewStateAware, Reducible

internal data class ExampleFooScreenState(
    val label: ReducibleValue<String> = ReducibleValue(""),
    val names: ReducibleValue<List<String>> = ReducibleValue.Uninitialized
) : Reducible

internal data class ExampleBarScreenState(
    val label: ReducibleValue<String> = ReducibleValue.Uninitialized,
    val numbers: ReducibleValue<List<Int>> = ReducibleValue.Uninitialized
) : Reducible

internal sealed class ExampleCallbackAction : HexagonCallbackAction, ReduceAction<ExampleState, ExampleStateReduceSpec> {
    object OnClickToggleButton : ExampleCallbackAction() {
        override val operation: ExampleStateReduceSpec.() -> Unit
            get() = {
                viewStateSpec = {
                    fooScreenStateSpec = {
                        label = LABEL_CLICKED
                    }
                }
            }
    }

    companion object {
        const val LABEL_CLICKED = "Clicked"
    }
}

internal sealed class ExampleMiddlewareAction : HexagonMiddlewareAction, ReduceAction<ExampleState, ExampleStateReduceSpec> {
    object ShowLoading : ExampleMiddlewareAction() {
        override val operation: ExampleStateReduceSpec.() -> Unit
            get() = {
                viewStateSpec = {
                    isShowLoading = true
                }
            }
    }

    object HideLoading : ExampleMiddlewareAction() {
        override val operation: ExampleStateReduceSpec.() -> Unit
            get() = {
                viewStateSpec = {
                    isShowLoading = false
                }
            }
    }

    class UpdateUserId(val newUserId: Int) : ExampleMiddlewareAction() {
        override val operation: ExampleStateReduceSpec.() -> Unit
            get() = {
                userId = newUserId
            }
    }

    class UpdateFooScreen(val newLabel: String, val newNames: List<String>) : ExampleMiddlewareAction() {
        override val operation: ExampleStateReduceSpec.() -> Unit
            get() = {
                viewStateSpec = {
                    fooScreenStateSpec = {
                        label = newLabel
                        names = newNames
                    }
                }
            }
    }

    class UpdateBarScreen(val newLabel: String, val newNumbers: List<Int>) : ExampleMiddlewareAction() {
        override val operation: ExampleStateReduceSpec.() -> Unit
            get() = {
                viewStateSpec = {
                    barScreenStateSpec = {
                        label = newLabel
                        numbers = newNumbers
                    }
                }
            }
    }
}

internal class ExampleReducer : HexagonReducer<ExampleState>,
    SpecBasedReducer<ExampleState, ExampleStateReduceSpec>(::ExampleStateReduceSpec) {

    override fun reduce(state: ExampleState, action: HexagonActionType): ExampleState =
        when (action) {
            is ExampleCallbackAction -> this(state, action)
            is ExampleMiddlewareAction -> this(state, action)

            else -> state
        }
}

internal class ExampleStateReduceSpec(snapshot: ExampleState) : ReduceSpec<ExampleState>(snapshot) {

    private var viewStateOp: ReduceOperation<ExampleViewStateReduceSpec.() -> Unit> = ReduceOperation.Keep
    var viewStateSpec by StateSpecDelegate(::viewStateOp::set)

    private var userIdOp: ReduceOperation<Int> = ReduceOperation.Keep
    var userId by snapshot.userId.reduceOp(::userIdOp::set)

    override fun ExampleState.apply(): ExampleState = this
        .update(viewStateOp) { spec -> copy(viewState = viewState.reduce(::ExampleViewStateReduceSpec, spec.current)) }
        .update(userIdOp) { new -> copy(userId = new) }
}

internal class ExampleViewStateReduceSpec(snapshot: ExampleViewState) : ReduceSpec<ExampleViewState>(snapshot) {

    private var fooScreenStateOp: ReduceOperation<FooScreenStateReduceSpec.() -> Unit> = ReduceOperation.Keep
    var fooScreenStateSpec by StateSpecDelegate(::fooScreenStateOp::set)

    private var barScreenStateOp: ReduceOperation<BarScreenStateReduceSpec.() -> Unit> = ReduceOperation.Keep
    var barScreenStateSpec by StateSpecDelegate(::barScreenStateOp::set)

    private var isShowLoadingOp: ReduceOperation<Boolean> = ReduceOperation.Keep
    var isShowLoading by snapshot.isShowLoading.reduceOp(::isShowLoadingOp::set)

    override fun ExampleViewState.apply(): ExampleViewState = this
        .update(isShowLoadingOp) { new -> copy(isShowLoading = new) }
        .update(fooScreenStateOp) { spec -> copy(fooScreenState = fooScreenState.reduce(::FooScreenStateReduceSpec, spec.current)) }
        .update(barScreenStateOp) { spec -> copy(barScreenState = barScreenState.reduce(::BarScreenStateReduceSpec, spec.current)) }
}

internal class FooScreenStateReduceSpec(snapshot: ExampleFooScreenState) : ReduceSpec<ExampleFooScreenState>(snapshot) {

    private var labelOp: ReduceOperation<String> = ReduceOperation.Keep
    var label by snapshot.label.reduceOp(::labelOp::set)

    private var namesOp: ReduceOperation<List<String>> = ReduceOperation.Keep
    var names by snapshot.names.reduceOp(::namesOp::set)

    override fun ExampleFooScreenState.apply(): ExampleFooScreenState = this
        .update(labelOp) { new -> copy(label = new) }
        .update(namesOp) { new -> copy(names = new) }
}

internal class BarScreenStateReduceSpec(snapshot: ExampleBarScreenState) : ReduceSpec<ExampleBarScreenState>(snapshot) {

    private var labelOp: ReduceOperation<String> = ReduceOperation.Keep
    var label by snapshot.label.reduceOp(::labelOp::set)

    private var numbersOp: ReduceOperation<List<Int>> = ReduceOperation.Keep
    var numbers by snapshot.numbers.reduceOp(::numbersOp::set)

    override fun ExampleBarScreenState.apply(): ExampleBarScreenState = this
        .update(labelOp) { new -> copy(label = new) }
        .update(numbersOp) { new -> copy(numbers = new) }
}