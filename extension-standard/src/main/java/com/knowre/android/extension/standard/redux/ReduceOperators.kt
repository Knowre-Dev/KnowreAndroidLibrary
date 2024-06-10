package com.knowre.android.extension.standard.redux

import com.knowre.android.extension.standard.redux.operation.ReduceOperation
import com.knowre.android.extension.standard.redux.spec.ReduceSpec
import com.knowre.android.extension.standard.redux.state.ReduceOnceValue
import com.knowre.android.extension.standard.redux.state.ReducibleValue

inline fun <R : Reducible, Spec : ReduceSpec<R>> R.reduce(
    spec: (R) -> Spec,
    action: ReduceAction<R, Spec>
): R = reduce(spec, action.operation)

inline fun <R : Reducible, Spec : ReduceSpec<R>> R.reduce(
    spec: (R) -> Spec,
    operation: Spec.() -> Unit
): R = spec(this).apply(operation)()

inline fun <T, R : Reducible> R.update(
    operation: ReduceOperation<T>,
    block: R.(ReducibleValue.Actual<T>) -> R
): R = when (operation) {
    ReduceOperation.Keep -> this
    is ReduceOperation.Update -> block(ReducibleValue.Actual(operation.next))
}

inline fun <T, R: Reducible> R.updateOnce(
    operation: ReduceOperation<T>,
    block: R.(ReducibleValue.Actual<ReduceOnceValue<T>>) -> R
): R = when (operation) {
    ReduceOperation.Keep -> this
    is ReduceOperation.Update -> block(
        ReducibleValue.Actual(ReduceOnceValue(operation.next))
    )
}