package com.knowre.android.extension.standard.redux.state

sealed class ReducibleValue<out T> {
    data class Actual<T>(val current: T) : ReducibleValue<T>() {
        override fun toString(): String = current.toString()
    }

    object Uninitialized : ReducibleValue<Nothing>() {
        override fun toString(): String = "Uninitialized"
    }

    companion object {
        operator fun <T> invoke(next: T): ReducibleValue<T> = Actual(next)
    }
}

val <T> ReducibleValue<T>.current: T
    get() = when (this) {
        is ReducibleValue.Actual -> current
        ReducibleValue.Uninitialized -> ReducibleValue.error(
            UNINITIALIZED_VALUE_ACCESS_EXCEPTION
        )
    }

fun <T> ReducibleValue<T>.peek(): Result<T> =
    runCatching { current }