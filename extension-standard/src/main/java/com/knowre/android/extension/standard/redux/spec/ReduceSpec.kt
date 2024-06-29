@file:Suppress("MemberVisibilityCanBePrivate")

package com.knowre.android.extension.standard.redux.spec

import com.knowre.android.extension.standard.redux.Reducible

@ReduceSpecDsl
abstract class ReduceSpec<R : Reducible>(protected val snapshot: R) : () -> R {

    protected abstract fun R.apply(): R

    final override operator fun invoke(): R = snapshot.apply()
}