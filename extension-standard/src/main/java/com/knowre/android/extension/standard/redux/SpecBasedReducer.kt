package com.knowre.android.extension.standard.redux

import com.knowre.android.extension.standard.redux.spec.ReduceSpec

@Suppress("MemberVisibilityCanBePrivate")
open class SpecBasedReducer<R : Reducible, Spec : ReduceSpec<R>>(
    protected val spec: (R) -> Spec
) : (R, ReduceAction<R, Spec>) -> R {

    final override operator fun invoke(reducible: R, reduceAction: ReduceAction<R, Spec>): R =
        reduce(reducible, reduceAction)

    private fun reduce(snapshot: R, reduceAction: ReduceAction<R, Spec>): R =
        snapshot.reduce(spec, reduceAction)
}