package com.knowre.android.extension.standard.redux.state

typealias ReduceOnce<T> = ReducibleValue<ReduceOnceValue<T>>

@JvmInline
value class ReduceOnceValue<out T>
@PublishedApi internal constructor(internal val value: T) {
    override fun toString(): String = value.toString()
}

val <T> ReduceOnce<T>.current: T
    get() = when (this) {
        is ReducibleValue.Actual -> current.value
        ReducibleValue.Uninitialized -> ReducibleValue.error(
            UNINITIALIZED_VALUE_ACCESS_EXCEPTION
        )
    }

fun <T> ReduceOnce<T>.peek(): Result<T> =
    runCatching { current }