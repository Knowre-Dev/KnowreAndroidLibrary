package com.knowre.android.extension.standard.redux.operation

import com.knowre.android.extension.standard.redux.Reducible
import com.knowre.android.extension.standard.redux.spec.ReduceSpec
import com.knowre.android.extension.standard.redux.state.ReduceOnceValue
import com.knowre.android.extension.standard.redux.state.ReducibleValue
import com.knowre.android.extension.standard.redux.state.current
import com.knowre.android.extension.standard.redux.state.error
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class ReduceOperation<out T> {
    data class Update<T>(val next: T) : ReduceOperation<T>()

    object Keep : ReduceOperation<Nothing>()
}

inline fun <T, R : Reducible, Spec : ReduceSpec<R>> ReducibleValue<T>.reduceOp(
    crossinline update: (ReduceOperation.Update<T>) -> Unit,
    crossinline get: ReducibleValue<T>.(thisRef: Spec) -> T = { _ -> current }
) = reduceOp(
    update = { _, op -> update(op) },
    get = get
)

inline fun <T, R : Reducible, Spec : ReduceSpec<R>> ReducibleValue<T>.reduceOp(
    crossinline update: (thisRef: Spec, ReduceOperation.Update<T>) -> Unit,
    crossinline get: ReducibleValue<T>.(thisRef: Spec) -> T = { _ -> current }
) = object : ReadWriteProperty<Spec, T> {
    override fun getValue(thisRef: Spec, property: KProperty<*>): T = get(thisRef)
    override fun setValue(thisRef: Spec, property: KProperty<*>, value: T) {
        update(thisRef, ReduceOperation.Update(value))
    }
}

inline fun <T, V : ReduceOnceValue<T>, R : Reducible, Spec : ReduceSpec<R>> ReducibleValue<V>.reduceOnceOp(
    crossinline update: (ReduceOperation.Update<T>) -> Unit,
    crossinline get: ReducibleValue<V>.(thisRef: Spec) -> T = { _ -> current },
    crossinline onConflict: (current: T, next: T) -> Unit = { current, next ->
        ReducibleValue.error(
            "ReduceOnce had already been reduced. (current = $current, attempt = $next)"
        )
    }
) = reduceOnceOp(
    update = { _, op -> update(op) },
    get = get,
    onConflict = onConflict
)

inline fun <T, V : ReduceOnceValue<T>, R : Reducible, Spec : ReduceSpec<R>> ReducibleValue<V>.reduceOnceOp(
    crossinline update: (thisRef: Spec, ReduceOperation.Update<T>) -> Unit,
    crossinline get: ReducibleValue<V>.(thisRef: Spec) -> T = { _ -> current },
    crossinline onConflict: (current: T, next: T) -> Unit = { current, next ->
        ReducibleValue.error(
            "ReduceOnce had already been reduced. (current = $current, attempt = $next)"
        )
    }
) = object : ReadWriteProperty<Spec, T> {
    override fun getValue(thisRef: Spec, property: KProperty<*>): T = get(thisRef)
    override fun setValue(thisRef: Spec, property: KProperty<*>, value: T) {
        when (this@reduceOnceOp) {
            ReducibleValue.Uninitialized -> update(thisRef, ReduceOperation.Update(value))
            is ReducibleValue.Actual -> onConflict(current.value, value)
        }
    }
}