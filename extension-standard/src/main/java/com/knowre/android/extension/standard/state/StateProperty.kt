package com.knowre.android.extension.standard.state

sealed class StateProperty<out T> {

    data class Data<T>(val value: T) : StateProperty<T>()

    object Uninitialized : StateProperty<Nothing>()

    companion object {
        operator fun <T> invoke(value: T): StateProperty<T> = Data(value)
    }
}