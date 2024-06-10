package com.knowre.android.extension.standard.redux.operation

sealed class ReduceOperation<out T> {
    data class Update<T>(val next: T) : ReduceOperation<T>()

    object Keep : ReduceOperation<Nothing>()
}