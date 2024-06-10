package com.knowre.android.extension.standard.redux.spec

import com.knowre.android.extension.standard.redux.Reducible
import com.knowre.android.extension.standard.redux.operation.ReduceOperation
import com.knowre.android.extension.standard.redux.state.ReduceOnceValue
import com.knowre.android.extension.standard.redux.state.ReducibleValue
import com.knowre.android.extension.standard.redux.state.current
import com.knowre.android.extension.standard.redux.state.error
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@ReduceSpecDsl
@Suppress("MemberVisibilityCanBePrivate")
abstract class ReduceSpec<R : Reducible>(protected val snapshot: R) : () -> R {

    protected abstract fun R.apply(): R

    final override operator fun invoke(): R = snapshot.apply()

    protected fun <T> ReducibleValue<T>.reduceProperty(
        update: (ReduceOperation.Update<T>) -> Unit
    ) = object : ReadWriteProperty<ReduceSpec<R>, T> {
        override fun getValue(thisRef: ReduceSpec<R>, property: KProperty<*>): T = current

        override fun setValue(thisRef: ReduceSpec<R>, property: KProperty<*>, value: T) {
            update(ReduceOperation.Update(value))
        }
    }

    protected fun <T, V : ReduceOnceValue<T>> ReducibleValue<V>.reduceOnceProperty(
        update: (ReduceOperation.Update<T>) -> Unit,
        onConflict: (current: T, next: T) -> Unit = { current, next ->
            ReducibleValue.error(
                "ReduceOnce had already been reduced. (current = $current, attempt = $next)"
            )
        }
    ) = object : ReadWriteProperty<ReduceSpec<R>, T> {
        override fun getValue(thisRef: ReduceSpec<R>, property: KProperty<*>): T = current

        override fun setValue(thisRef: ReduceSpec<R>, property: KProperty<*>, value: T) {
            when (this@reduceOnceProperty) {
                ReducibleValue.Uninitialized -> update(ReduceOperation.Update(value))
                is ReducibleValue.Actual -> onConflict(current.value, value)
            }
        }
    }
}