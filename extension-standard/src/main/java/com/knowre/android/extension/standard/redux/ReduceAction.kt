package com.knowre.android.extension.standard.redux

import com.knowre.android.extension.standard.redux.spec.ReduceSpec

interface ReduceAction<R : Reducible, Spec : ReduceSpec<R>> {
    val operation: Spec.() -> Unit
}